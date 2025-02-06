package com.example.login.service;

import com.example.login.dto.request.AlterPassRequest;
import com.example.login.dto.response.TokenResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;

public interface AuthenticationService {

    TokenResponse login(Authentication authentication);

    void logout(HttpServletRequest request);

    void forgotPassword(String email);

    void resetPassword(AlterPassRequest alterPassRequest);
}
