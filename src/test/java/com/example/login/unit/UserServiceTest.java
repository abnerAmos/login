package com.example.login.unit;

import com.example.login.dto.request.UserRequest;
import com.example.login.enums.Role;
import com.example.login.exception.BadRequestException;
import com.example.login.model.User;
import com.example.login.repository.UserRepository;
import com.example.login.service.EmailService;
import com.example.login.service.impl.UserServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static com.example.login.factory.UserFactory.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

    @Test
    @DisplayName("Deve registrar um usuário com sucesso")
    public void testRegisterUser_UserNotExist_Success() {
        // Dado: um novo usuário para cadastro
        UserRequest userRequest = new UserRequest(false, USERNAME, EMAIL, "ADMIN", PASS);

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

    @Test
    @DisplayName("Não deve enviar e-mail de registro se o usuário for experimental")
    public void testRegisterUser_UserExperimental_NotSendEmail_Success() {
        UserRequest userRequest = new UserRequest(true, USERNAME, EMAIL, "ADMIN", PASS);

        when(userRepository.existsByEmail(userRequest.email())).thenReturn(false);
        when(passEncoder.encode(userRequest.password())).thenReturn(ENCODE_PASS);

        userService.registerUser(userRequest);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertEquals(EMAIL, savedUser.getEmail());
        assertEquals(ENCODE_PASS, savedUser.getPassword());
        assertTrue(savedUser.isEnabled());
        assertEquals(Role.ADMIN, savedUser.getRole());

        // Verifica que o e-mail de registro NÃO foi enviado
        verify(emailService, never()).sendRegisterEmail(userRequest.email());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar registrar um usuário já existente")
    public void testRegisterUser_UserExist_Error() {
        UserRequest userRequest = new UserRequest(false, USERNAME, EMAIL, "ADMIN", PASS);

        when(userRepository.existsByEmail(userRequest.email())).thenReturn(true);

        assertThrows(BadRequestException.class, () -> userService.registerUser(userRequest));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar registrar um usuário com função inexistente")
    public void testRegisterUser_NotExistRole_Error() {
        UserRequest userRequest = new UserRequest(false, USERNAME, EMAIL, "?", PASS);

        when(userRepository.existsByEmail(userRequest.email())).thenReturn(false);

        assertThrows(BadRequestException.class, () -> userService.registerUser(userRequest));
    }
}
