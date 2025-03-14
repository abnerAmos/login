package com.example.login.unit;

import com.example.login.cache.TokenCache;
import com.example.login.dto.response.TokenData;
import com.example.login.dto.response.TokenResponse;
import com.example.login.factory.UserFactory;
import com.example.login.model.User;
import com.example.login.repository.UserRepository;
import com.example.login.security.TokenService;
import com.example.login.service.impl.AuthenticationServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static com.example.login.factory.UserFactory.EMAIL;
import static com.example.login.factory.UserFactory.ENCODE_PASS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private TokenService tokenService;

    @Mock
    private TokenCache tokenCache;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    @Test
    @DisplayName("Deve carregar usuário com sucesso")
    public void testLoadUserByUsername_Success() {
        User user = UserFactory.createUser(1L);
        when(userRepository.findByEmail(EMAIL)).thenReturn(user);

        UserDetails userDetails = authenticationService.loadUserByUsername(EMAIL);

        assertNotNull(userDetails);
        assertEquals(EMAIL, userDetails.getUsername());
        assertEquals(ENCODE_PASS, userDetails.getPassword());
    }

    @Test
    @DisplayName("Deve lançar UsernameNotFoundException ao tentar carregar usuário")
    public void testLoadUserByUsername_Error() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(null);

        assertThrows(UsernameNotFoundException.class, () -> authenticationService.loadUserByUsername(EMAIL));
    }

    @Test
    @DisplayName("Deve gerar novos Access e Refresh Tokens quando não há Refresh Token existente")
    public void testLogin_WithoutExistingRefreshToken() {
        User user = UserFactory.createUser(1L);

        when(authentication.getPrincipal()).thenReturn(user);
        when(tokenCache.getExistingToken(eq(user.getId()), anyString())).thenReturn(null);
        when(tokenService.generateToken(user, "accessToken")).thenReturn(new TokenData("newAccessToken", 3600));
        when(tokenService.generateToken(user, "refreshToken")).thenReturn(new TokenData("newRefreshToken", 7200));

        TokenResponse tokenResponse = authenticationService.login(authentication);

        assertNotNull(tokenResponse);
        assertEquals("newAccessToken", tokenResponse.accessToken());
        assertEquals("newRefreshToken", tokenResponse.refreshToken());
        verify(tokenCache).storeToken(eq(1L), eq("newAccessToken"), anyLong(), eq("accessToken"));
        verify(tokenCache).storeToken(eq(1L), eq("newRefreshToken"), anyLong(), eq("refreshToken"));
    }

    @Test
    @DisplayName("Deve gerar novo Access Token utilizando Refresh Token existente")
    public void testLogin_WithExistingRefreshToken() {
        User user = UserFactory.createUser(1L);

        when(authentication.getPrincipal()).thenReturn(user);
        when(tokenService.generateToken(user, "accessToken")).thenReturn(new TokenData("newAccessToken", 3600));
        when(tokenCache.getExistingToken(eq(user.getId()), anyString())).thenReturn("existingRefreshToken");
        when(tokenCache.isTokenInvalidated("existingRefreshToken")).thenReturn(false);


        TokenResponse tokenResponse = authenticationService.login(authentication);

        assertNotNull(tokenResponse);
        assertEquals("newAccessToken", tokenResponse.accessToken());
        assertEquals("existingRefreshToken", tokenResponse.refreshToken());
        verify(tokenCache).storeToken(eq(1L), eq("newAccessToken"), anyLong(), eq("accessToken"));
        verify(tokenCache, never()).storeToken(eq(1L), eq("existingRefreshToken"), anyLong(), eq("refreshToken"));
    }

    @Test
    @DisplayName("Deve gerar novos tokens de acesso e refresh quando o refresh token é válido")
    public void testRefreshToken_Success() {
        String refreshTokenRequest = "validRefreshToken";
        User user = UserFactory.createUser(1L);
        TokenData newAccessToken = new TokenData("newAccessToken", 3600);
        TokenData newRefreshToken = new TokenData("newRefreshToken", 7200);

        when(tokenCache.isTokenInvalidated(refreshTokenRequest)).thenReturn(false);
        when(tokenService.getSubject(refreshTokenRequest, "refreshToken")).thenReturn(user.getUsername());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(tokenService.generateToken(user, "accessToken")).thenReturn(newAccessToken);
        when(tokenService.generateToken(user, "refreshToken")).thenReturn(newRefreshToken);

        TokenResponse tokenResponse = authenticationService.refreshToken(refreshTokenRequest);

        assertNotNull(tokenResponse);
        assertEquals("newAccessToken", tokenResponse.accessToken());
        assertEquals("newRefreshToken", tokenResponse.refreshToken());
        verify(tokenCache).storeToken(user.getId(), "newAccessToken", newAccessToken.expiration(), "accessToken");
        verify(tokenCache).storeToken(user.getId(), "newRefreshToken", newRefreshToken.expiration(), "refreshToken");
    }

    @Test
    @DisplayName("Deve lançar AuthenticationException quando o token é inválido ou expirado")
    public void testRefreshToken_InvalidOrExpiredToken() {
        String invalidRefreshToken = "invalidOrExpiredToken";

        when(tokenCache.isTokenInvalidated(invalidRefreshToken)).thenReturn(true);

        assertThrows(AuthenticationException.class,
                () -> authenticationService.refreshToken(invalidRefreshToken));
    }

    @Test
    @DisplayName("Deve lançar UsernameNotFoundException quando o usuário não é encontrado")
    public void testRefreshToken_UserNotFound() {
        String validRefreshToken = "validRefreshToken";
        User user = UserFactory.createUser(1L);

        when(tokenCache.isTokenInvalidated(validRefreshToken)).thenReturn(false);
        when(tokenService.getSubject(validRefreshToken, "refreshToken")).thenReturn(user.getUsername());
        when(userRepository.findByEmail(EMAIL)).thenReturn(null);

        assertThrows(UsernameNotFoundException.class,
                () -> authenticationService.refreshToken(validRefreshToken));
    }
}
