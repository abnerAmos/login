package com.example.login.dto.request;

import com.example.login.validation.ValidPassword;

public record NewUserRequest(
        String username,
        String email,
        String role,
        @ValidPassword String password) {
}
