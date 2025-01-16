package com.example.login.service.impl;

import com.example.login.cache.ValidationCodeCache;
import com.example.login.dto.request.AlterPassRequest;
import com.example.login.exception.BadRequestException;
import com.example.login.model.User;
import com.example.login.repository.UserRepository;
import com.example.login.security.TokenService;
import com.example.login.service.AuthenticationService;
import com.example.login.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.example.login.cache.ValidationCodeCache.CODE_LENGTH;

/**
 * Serviço responsável pela autenticação de usuários na aplicação.
 *
 * <p>Esta classe implementa a interface {@link UserDetailsService} do Spring Security,
 * que é utilizada pelo {@link AuthenticationManager} para carregar os detalhes de um usuário
 * com base no seu nome de usuário (e-mail, neste caso) durante o processo de autenticação.
 *
 * <p>O método {@code loadUserByUsername} busca o usuário no banco de dados usando o repositório {@link UserRepository}.
 * Caso o usuário não seja encontrado, é lançada uma exceção {@link UsernameNotFoundException},
 * que indica falha na autenticação.
 *
 * <p>Esta classe é marcada como um {@code @Service}, tornando-a disponível para injeção de dependência
 * e permitindo que o {@link AuthenticationManager} a utilize automaticamente durante o processo de autenticação.
 *
 * @see UserDetailsService
 * @see AuthenticationManager
 */
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements UserDetailsService, AuthenticationService {

    private final UserRepository userRepository;

    private final ValidationCodeCache validationCodeCache;

    private final EmailService emailService;

    private final TokenService tokenService;

    private final PasswordEncoder passEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return Optional.of(userRepository.findByEmail(username))
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com o e-mail: " + username));
    }

    @Override
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new BadRequestException("Usuário não encontrado.");
        }

        String resetCode = validationCodeCache.generateValidationCode(email);
        String resetToken = tokenService.generateToken(user);

        String resetLink = "http://localhost:8080/auth/reset-password?token=" + resetToken + resetCode;
        String subject = "Redefinição de Senha";
        String text = "Clique no link para redefinir sua senha: " + resetLink + "<br><br>"
                + "<b>Este link é exclusivo e deve ser usado apenas por você. Não o compartilhe com ninguém.<b>";

        emailService.sendValidationEmail(email, subject, text);
    }

    @Override
    @Transactional
    public void resetPassword(AlterPassRequest recovery) {
        String code = recovery.code().substring(recovery.code().length() -CODE_LENGTH);
        String token = recovery.code().substring(0, recovery.code().length() -CODE_LENGTH);

        String email = tokenService.getSubject(token);
        var user = userRepository.findByEmail(email);
        if (user == null) {
            throw new BadRequestException("Usuário não encontrado.");
        }

        String cachedCode = validationCodeCache.getValidationCode(email);
        if (!code.equals(cachedCode)) {
            throw new BadRequestException("Código de validação expirado ou inválido.");
        }

        validationCodeCache.invalidateValidationCode(email);

        user.setPassword(passEncoder.encode(recovery.password()));
        user.setLastAlterPass(LocalDateTime.now());
    }
}
