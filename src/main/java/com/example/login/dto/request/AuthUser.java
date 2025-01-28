package com.example.login.dto.request;

public record AuthUser(
        Long id,
        String username,
        String role) {
}
