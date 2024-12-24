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

    /**
     * Registra um novo usuário na aplicação.
     *
     * @param user Contém os dados do usuário a ser registrado.
     *             A validação é realizada usando as anotações de validação (@Valid) no objeto.
     * @return Uma resposta HTTP contendo o status 201 (Created) e uma mensagem de sucesso.
     *         Caso ocorra alguma falha na validação ou processamento, uma exceção será lançada.
     */
    @PostMapping("/register")
    public ResponseEntity<HttpSucessResponse> register(@RequestBody @Valid User user) {
        var httpResponse = new HttpSucessResponse(HttpStatus.CREATED, "Cadastro efetuado com sucesso");
        userService.registerUser(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(httpResponse);
    }

    /**
     * Busca as informações de um usuário específico com base no ID fornecido.
     *
     * @param id O identificador único (ID) do usuário a ser buscado.
     * @return Uma resposta HTTP contendo o status 200 (OK) e os dados do usuário.
     *         Caso o usuário não seja encontrado, uma exceção será lançada.
     */
    @GetMapping()
    public ResponseEntity<User> findUser(@PathVariable Long id) {
        var user = userService.findUser(id);

        return ResponseEntity.status(HttpStatus.OK).body(user);
    }

}
