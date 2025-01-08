package com.example.login.controller;

import com.example.login.dto.request.LoginRequest;
import com.example.login.dto.response.HttpSucessResponse;
import com.example.login.dto.response.TokenResponse;
import com.example.login.model.User;
import com.example.login.security.TokenService;
import com.example.login.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;

    private final TokenService tokenService;

    private final EmailService emailService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest login) {
        var authenticationToken = new UsernamePasswordAuthenticationToken(login.email(), login.password());
        var authentication =  authenticationManager.authenticate(authenticationToken);
        var token = tokenService.generateToken((User) authentication.getPrincipal());

        return ResponseEntity.ok(new TokenResponse(token));
    }

    @PostMapping("/validate-code")
    public ResponseEntity<HttpSucessResponse> validateCode(@RequestParam String email, @RequestParam String code) {
        emailService.validationCode(email, code);

        var httpResponse = new HttpSucessResponse(HttpStatus.OK, "Validação concluída com sucesso. Você já pode fazer login.");
        return ResponseEntity.ok().body(httpResponse);
    }

}
