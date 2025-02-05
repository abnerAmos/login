package com.example.login.controller;

import com.example.login.dto.request.UserRequest;
import com.example.login.dto.response.HttpSuccessResponse;
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

    /**
     * Registra um novo usuário na aplicação.
     *
     * @param user Contém os dados do usuário a ser registrado.
     *             A validação é realizada usando as anotações de validação (@Valid) no objeto.
     * @return Uma resposta HTTP contendo o status 201 (Created) e uma mensagem de sucesso.
     *         Caso ocorra alguma falha na validação ou processamento, uma exceção será lançada.
     */
    @PostMapping("/register")
    public ResponseEntity<HttpSuccessResponse> register(@RequestBody @Valid UserRequest user) {
        userService.registerUser(user);

        var httpResponse = new HttpSuccessResponse(HttpStatus.CREATED, "Cadastro efetuado com sucesso");
        return ResponseEntity.status(HttpStatus.CREATED).body(httpResponse);
    }

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
