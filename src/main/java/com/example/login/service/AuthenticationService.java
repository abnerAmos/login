package com.example.login.service;

import com.example.login.dto.request.AlterPassRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;

public interface AuthenticationService {

    String login(Authentication authentication);

    void logout(HttpServletRequest request);

    void forgotPassword(String email);

    void resetPassword(AlterPassRequest alterPassRequest);
}
