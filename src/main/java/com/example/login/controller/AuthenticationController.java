package com.example.login.controller;

import com.example.login.dto.request.AlterPassRequest;
import com.example.login.dto.request.CodeRequest;
import com.example.login.dto.request.LoginRequest;
import com.example.login.dto.response.HttpSucessResponse;
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

    @PostMapping("/logout")
    public ResponseEntity<HttpSucessResponse> logout(HttpServletRequest request) {
        authenticationService.logout(request);

        var httpResponse = new HttpSucessResponse("Logout realizado com sucesso");
        return ResponseEntity.ok().body(httpResponse);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<HttpSucessResponse> forgotPassword(@RequestParam String email) {
        authenticationService.forgotPassword(email);

        var httpResponse = new HttpSucessResponse("Um e-mail foi enviado com instruções para redefinir sua senha.");
        return ResponseEntity.ok().body(httpResponse);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<HttpSucessResponse> resetPassword(@RequestBody AlterPassRequest alterPassRequest) {
        authenticationService.resetPassword(alterPassRequest);

        var httpResponse = new HttpSucessResponse("Sua senha foi alterada com sucesso.");
        return ResponseEntity.ok().body(httpResponse);
    }

    /**
     * Valida o código de verificação enviado por e-mail para o usuário.
     * <p>
     * Este endpoint verifica se o código fornecido corresponde ao código associado ao e-mail do usuário.
     * Se a validação for bem-sucedida, o usuário poderá prosseguir para fazer login.
     *
     * @param codeRequest Objeto contendo e-mail do usuário e código para validação.
     * @return Uma resposta HTTP 200 com uma mensagem indicando que a validação foi concluída com sucesso.
     */
    @PostMapping("/validate-code")
    public ResponseEntity<HttpSucessResponse> validateCode(@RequestBody CodeRequest codeRequest) {
        emailService.validationCode(codeRequest);

        var httpResponse = new HttpSucessResponse("Validação concluída com sucesso. Você já pode fazer login.");
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
    public ResponseEntity<HttpSucessResponse> refreshCode(@RequestParam String email) {
        emailService.sendRefreshCode(email);

        var httpResponse = new HttpSucessResponse("Novo código de validação enviado.");
        return ResponseEntity.ok().body(httpResponse);
    }

}
