package com.example.login.enums;

import com.example.login.exception.BadRequestException;
import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    ADMIN,
    USER;

    @Override
    public String getAuthority() {
        return "ROLE_" + this.name();
    }

    public static Role getRole(String typeRole) {
        for (Role role : values()) {
            if (role.name().equals(typeRole)) {
                return role;
            }
        }
        throw new BadRequestException("Função " + typeRole + " não localizada");
    }
}
