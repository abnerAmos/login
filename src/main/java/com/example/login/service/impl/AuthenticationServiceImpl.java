package com.example.login.service.impl;

import com.example.login.cache.TokenCache;
import com.example.login.cache.ValidationCodeCache;
import com.example.login.dto.request.AlterPassRequest;
import com.example.login.exception.BadRequestException;
import com.example.login.exception.ForbiddenException;
import com.example.login.model.User;
import com.example.login.repository.UserRepository;
import com.example.login.security.TokenService;
import com.example.login.service.AuthenticationService;
import com.example.login.service.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
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

    private final PasswordEncoder passEncoder;
    private final UserRepository userRepository;
    private final ValidationCodeCache validationCodeCache;
    private final TokenCache tokenCache;
    private final EmailService emailService;
    private final TokenService tokenService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return getUser(username);
    }

    @Override
    public String login(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        String existingToken = tokenCache.getExistingToken(user.getId());

        if (existingToken != null) {
            long expiration = tokenService.getExpiration(existingToken);
            tokenCache.invalidateToken(existingToken, expiration);
        }

        String newToken = tokenService.generateToken(user);
        tokenCache.storeToken(user.getId(), newToken, tokenService.getExpiration(newToken));

        return newToken;
    }

    @Override
    public void logout(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || authorizationHeader.isEmpty()) {
            throw new ForbiddenException("Token não fornecido");
        }

        String token = authorizationHeader.replace("Bearer ", "").trim();
        long expiration = tokenService.getExpiration(token);
        tokenCache.invalidateToken(token, expiration);
    }

    @Override
    public void forgotPassword(String email) {
        User user = getUser(email);

        if (user.getLastAlterPass() != null
                && user.getLastAlterPass().isAfter(LocalDateTime.now().minusHours(1))) {
            throw new BadRequestException("É necessário aguardar pelo menos 1 hora para alterar a senha novamente.");
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
        User user = getUser(email);

        if (passEncoder.matches(recovery.password(), user.getPassword()) ||
                (user.getLastPassword() != null
                        && passEncoder.matches(recovery.password(), user.getLastPassword()))) {
            throw new BadRequestException("Senha já utilizada, insira uma senha diferente.");
        }

        String cachedCode = validationCodeCache.getValidationCode(email);
        if (!code.equals(cachedCode)) {
            throw new BadRequestException("Código de validação expirado ou inválido.");
        }

        validationCodeCache.invalidateValidationCode(email);

        user.setLastPassword(user.getPassword());
        user.setPassword(passEncoder.encode(recovery.password()));
        user.setLastAlterPass(LocalDateTime.now());
    }

    private User getUser(String username) {
        return Optional.of(userRepository.findByEmail(username))
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado."));
    }
}
