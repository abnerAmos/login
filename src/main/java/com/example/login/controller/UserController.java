package com.example.login.controller;

import com.example.login.dto.response.HttpSucessResponse;
import com.example.login.model.User;
import com.example.login.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<HttpSucessResponse> register(@RequestBody @Valid User user) {
        var httpResponse = new HttpSucessResponse(HttpStatus.CREATED, "Cadastro efetuado com sucesso");
        userService.registerUser(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(httpResponse);
    }

    @GetMapping()
    public ResponseEntity<User> findUser(@PathVariable Long id) {
        var user = userService.findUser(id);

        return ResponseEntity.status(HttpStatus.OK).body(user);
    }

}
