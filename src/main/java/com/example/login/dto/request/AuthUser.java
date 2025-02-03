package com.example.login.dto.request;

import com.example.login.enums.Role;

public record AuthUser(
        Long id,
        String username,
        Role role) {
}
