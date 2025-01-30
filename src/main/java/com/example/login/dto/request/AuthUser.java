package com.example.login.dto.request;

import com.example.login.enums.Role;

import java.util.Set;

public record AuthUser(
        Long id,
        String username,
        Set<Role> roles) {
}
