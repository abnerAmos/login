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

    /**
     * Carrega os detalhes do usuário com base no nome de usuário fornecido.
     * <p>
     * Este método busca um usuário no sistema utilizando o nome de usuário fornecido (geralmente e-mail) e retorna os detalhes do usuário para autenticação.
     * Caso o usuário não seja encontrado, uma exceção {@link UsernameNotFoundException} será lançada.
     *
     * @param username O nome de usuário (normalmente o e-mail) do usuário a ser autenticado.
     * @return Um objeto {@link UserDetails} contendo os detalhes do usuário.
     * @throws UsernameNotFoundException Caso o usuário não seja encontrado.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return getUser(username);
    }

    /**
     * Realiza o login do usuário e gera um novo token JWT.
     * <p>
     * Este método verifica se o usuário já possui um token existente.
     * Se houver, o token anterior é invalidado.
     * Em seguida, um novo token JWT é gerado e armazenado no cache de tokens.
     *
     * @param authentication A autenticação contendo as credenciais do usuário.
     * @return O novo token JWT gerado para o usuário.
     */
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

    /**
     * Realiza o logout do usuário, invalidando o token JWT associado.
     * <p>
     * Este método retira o token JWT fornecido na requisição de autorização e o invalida, garantindo que o usuário não tenha mais acesso aos recursos protegidos.
     * Se o token não for fornecido ou estiver malformado, será lançada uma exceção {@link ForbiddenException}.
     *
     * @param request A requisição HTTP contendo o token JWT.
     * @throws ForbiddenException Caso o token não seja fornecido ou seja inválido.
     */
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

    /**
     * Inicia o processo de recuperação de senha, enviando um código de redefinição para o e-mail do usuário.
     * <p>
     * Este método verifica se o usuário solicitou recentemente uma mudança de senha (verificando a hora da última alteração).
     * Caso o tempo limite tenha sido excedido, um e-mail com um link de redefinição de senha será enviado para o usuário.
     *
     * @param email O e-mail do usuário que solicitou a recuperação de senha.
     * @throws BadRequestException Caso o usuário tenha solicitado alteração de senha recentemente.
     */
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

    /**
     * Redefine a senha do usuário.
     * <p>
     * Este método valida o código de redefinição fornecido pelo usuário e altera sua senha. Caso a senha fornecida seja a mesma que a anterior, ou se o código de validação for inválido ou expirado, uma exceção será gerada.
     * Após a redefinição da senha, o usuário tem o histórico da última senha e a data da última alteração de senha atualizados.
     *
     * @param recovery Objeto contendo os dados necessários para a recuperação de senha, incluindo o código de validação e a nova senha.
     * @throws BadRequestException Caso o código de validação seja inválido ou expirado, ou se a nova senha for igual à anterior.
     */
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
