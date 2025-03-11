package com.example.login.unit;

import com.example.login.factory.UserFactory;
import com.example.login.model.User;
import com.example.login.repository.UserRepository;
import com.example.login.service.impl.AuthenticationServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static com.example.login.factory.UserFactory.EMAIL;
import static com.example.login.factory.UserFactory.ENCODE_PASS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthenticationServiceImpl userDetailsService;

    @Test
    @DisplayName("Deve carregar usuário com sucesso")
    public void testLoadUserByUsername_Success() {
        User user = UserFactory.createUser(1L);
        when(userRepository.findByEmail(EMAIL)).thenReturn(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(EMAIL);

        assertNotNull(userDetails);
        assertEquals(EMAIL, userDetails.getUsername());
        assertEquals(ENCODE_PASS, userDetails.getPassword());
    }

    @Test
    @DisplayName("Deve lançar excessão ao tentar carregar usuário")
    public void testLoadUserByUsername_Error() {
        User user = UserFactory.createUser(1L);
        when(userRepository.findByEmail(EMAIL)).thenReturn(null);

        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername(EMAIL));
    }
}
