package com.example.login.controller;

import com.example.login.dto.request.LoginRequest;
import com.example.login.dto.request.RegisterDTO;
import com.example.login.dto.response.HttpSucessResponse;
import com.example.login.dto.response.TokenResponse;
import com.example.login.model.User;
import com.example.login.security.TokenService;
import com.example.login.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;

    private final TokenService tokenService;

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest login) {
        var authenticationToken = new UsernamePasswordAuthenticationToken(login.email(), login.password());
        var authentication =  authenticationManager.authenticate(authenticationToken);
        var token = tokenService.generateToken((User) authentication.getPrincipal());

        return ResponseEntity.ok(new TokenResponse(token));
    }

    @PostMapping("/register")
    public ResponseEntity<HttpSucessResponse> register(@RequestBody RegisterDTO registerDTO) {
        var httpResponse = new HttpSucessResponse(HttpStatus.OK, "Cadastro efetuado com sucesso");
        userService.registerUser(registerDTO);

        return ResponseEntity.ok().body(httpResponse);
    }

}
