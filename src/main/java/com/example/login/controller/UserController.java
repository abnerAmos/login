package com.example.login.controller;

import com.example.login.model.User;
import com.example.login.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Busca as informações do usuário logado.
     *
     * @return Uma resposta HTTP contendo o status 200 (OK) e os dados do usuário.
     *         Caso o usuário não seja encontrado, uma exceção será lançada.
     */
    @GetMapping(value = {"", "/view/{view}"})
    public ResponseEntity<User> findUser() {
        var user = userService.findUser();

        return ResponseEntity.status(HttpStatus.OK).body(user);
    }

}
