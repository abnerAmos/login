package com.example.login.enums;

public enum Role {
    ADMIN,
    USER;

    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}
