package com.example.login.controller;

import com.example.login.dto.request.RegisterDTO;
import com.example.login.dto.response.HttpSucessResponse;
import com.example.login.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<HttpSucessResponse> register(@RequestBody RegisterDTO registerDTO) {
        var httpResponse = new HttpSucessResponse(HttpStatus.CREATED, "Cadastro efetuado com sucesso");
        userService.registerUser(registerDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(httpResponse);
    }

}
