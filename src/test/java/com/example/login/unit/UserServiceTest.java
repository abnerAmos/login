package com.example.login.unit;

import com.example.login.dto.request.UserRequest;
import com.example.login.enums.Role;
import com.example.login.model.User;
import com.example.login.repository.UserRepository;
import com.example.login.service.EmailService;
import com.example.login.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserServiceImpl userService;

    public static final String USERNAME = "Fulano de Tal";
    public static final String EMAIL = "fulano@email.com";
    public static final String PASS = "Seph!r0t";
    public static final String ENCODE_PASS = "1!2@3#4$_";

    @Test
    public void testRegisterUser_Success() {
        // Dado: um novo usuário para cadastro
        UserRequest userRequest = new UserRequest(USERNAME, EMAIL, "ADMIN", PASS);

        // Simula que não existe um usuário com este e-mail
        when(userRepository.existsByEmail(userRequest.email())).thenReturn(false);
        // Simula a codificação da senha
        when(passEncoder.encode(userRequest.password())).thenReturn(ENCODE_PASS);

        // Quando: chama o método de registro
        userService.registerUser(userRequest);

        // Então: verifica se o usuário foi salvo com os valores corretos
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertEquals(EMAIL, savedUser.getEmail());
        assertEquals(ENCODE_PASS, savedUser.getPassword());
        assertFalse(savedUser.isEnabled()); // Verifica se o usuário foi salvo como não habilitado
        // Se Role.getRole("USER") retornar Role.USER, então:
        assertEquals(Role.ADMIN, savedUser.getRole());

        // Verifica se o e-mail de validação foi enviado
        verify(emailService).sendRegisterEmail(userRequest.email());
    }
}
