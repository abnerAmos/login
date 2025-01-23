package com.example.login.controller;

import com.example.login.dto.request.AlterPassRequest;
import com.example.login.dto.request.LoginRequest;
import com.example.login.dto.response.HttpSuccessResponse;
import com.example.login.dto.response.TokenResponse;
import com.example.login.security.TokenService;
import com.example.login.service.AuthenticationService;
import com.example.login.service.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final AuthenticationService authenticationService;
    private final TokenService tokenService;
    private final EmailService emailService;

    /**
     * Autentica o usuário e retorna um token JWT para acesso às rotas protegidas.
     * <p>
     * Este endpoint recebe as credenciais do usuário no corpo da requisição, valida as credenciais,
     * e, caso sejam válidas, retorna um token JWT que pode ser utilizado para autenticação em requisições subsequentes.
     *
     * @param login Um objeto contendo o e-mail e a senha do usuário.
     * @return Uma resposta HTTP 200 contendo o token JWT, caso a autenticação seja bem-sucedida.
     */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest login) {
        var authenticationToken = new UsernamePasswordAuthenticationToken(login.email(), login.password());
        var authentication = authenticationManager.authenticate(authenticationToken);
        var token = authenticationService.login(authentication);

        return ResponseEntity.ok(new TokenResponse(token));
    }

    /**
     * Realiza o logout do usuário.
     * <p>
     * Este endpoint finaliza a sessão do usuário, invalidando seu token JWT para impedir o acesso às rotas protegidas.
     *
     * @param request A requisição HTTP contendo os dados necessários para realizar o logout.
     * @return Uma resposta HTTP 200 com uma mensagem de sucesso indicando que o logout foi realizado.
     */
    @PostMapping("/logout")
    public ResponseEntity<HttpSuccessResponse> logout(HttpServletRequest request) {
        authenticationService.logout(request);

        var httpResponse = new HttpSuccessResponse("Logout realizado com sucesso");
        return ResponseEntity.ok().body(httpResponse);
    }

    /**
     * Inicia o processo de recuperação de senha enviando um e-mail com as instruções.
     * <p>
     * Este endpoint é utilizado quando o usuário esquece sua senha. Um e-mail será enviado com instruções para a redefinição da senha.
     *
     * @param email O e-mail do usuário para o qual as instruções serão enviadas.
     * @return Uma resposta HTTP 200 com uma mensagem indicando que o e-mail com instruções foi enviado.
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<HttpSuccessResponse> forgotPassword(@RequestParam String email) {
        authenticationService.forgotPassword(email);

        var httpResponse = new HttpSuccessResponse("Um e-mail foi enviado com instruções para redefinir sua senha.");
        return ResponseEntity.ok().body(httpResponse);
    }

    /**
     * Redefine a senha do usuário.
     * <p>
     * Este endpoint permite que o usuário defina uma nova senha após seguir o processo de recuperação de senha.
     * O novo valor de senha é enviado no corpo da requisição.
     *
     * @param alterPassRequest Objeto contendo os dados para a alteração de senha.
     * @return Uma resposta HTTP 200 com uma mensagem confirmando que a senha foi alterada com sucesso.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<HttpSuccessResponse> resetPassword(@RequestBody AlterPassRequest alterPassRequest) {
        authenticationService.resetPassword(alterPassRequest);

        var httpResponse = new HttpSuccessResponse("Sua senha foi alterada com sucesso.");
        return ResponseEntity.ok().body(httpResponse);
    }

    /**
     * Valida o código de verificação enviado por e-mail para o usuário.
     * <p>
     * Este endpoint verifica se o código fornecido corresponde ao código associado ao e-mail do usuário.
     * Se a validação for bem-sucedida, o usuário poderá prosseguir para fazer login.
     *
     * @param email do usuário para validação.
     * @param code código para validação.
     * @return Uma resposta HTTP 200 com uma mensagem indicando que a validação foi concluída com sucesso.
     */
    @PostMapping("/validate-code")
    public ResponseEntity<HttpSuccessResponse> validateCode(@RequestParam String email, @RequestParam String code) {
        emailService.validationCode(email, code);

        var httpResponse = new HttpSuccessResponse("Validação concluída com sucesso. Você já pode fazer login.");
        return ResponseEntity.ok().body(httpResponse);
    }

    /**
     * Gera e envia um novo código de validação para o e-mail do usuário.
     * <p>
     * Este endpoint é usado quando o usuário solicita um novo código de validação,
     * geralmente porque o código anterior expirou ou foi perdido.
     *
     * @param email O e-mail do usuário para o qual o novo código de validação será enviado.
     * @return Uma resposta HTTP 200 com uma mensagem indicando que o novo código foi enviado com sucesso.
     */
    @PostMapping("/refresh-code")
    public ResponseEntity<HttpSuccessResponse> refreshCode(@RequestParam String email) {
        emailService.sendRefreshCode(email);

        var httpResponse = new HttpSuccessResponse("Novo código de validação enviado.");
        return ResponseEntity.ok().body(httpResponse);
    }

}
