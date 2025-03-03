package com.example.login.dto.request;

import com.example.login.util.Sensitive;
import com.example.login.validation.ValidPassword;

public record UserRequest(
        Boolean isExperimental,
        String username,
        String email,
        String role,
        @ValidPassword @Sensitive String password) {
}
