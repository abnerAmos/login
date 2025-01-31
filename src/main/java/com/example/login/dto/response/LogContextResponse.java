package com.example.login.dto.response;

import com.example.login.enums.Role;

import java.time.LocalDateTime;
import java.util.Set;

public record LogContextResponse(
        String className,
        String methodName,
        LocalDateTime startTime,
        Object[] parameters,
        Long userId,
        Set<Role> roles) {
}
