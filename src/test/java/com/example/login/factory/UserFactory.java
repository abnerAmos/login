package com.example.login.factory;

import com.example.login.enums.Role;
import com.example.login.model.User;

import java.time.LocalDateTime;

public class UserFactory {

    public static final String USERNAME = "Fulano de Tal";
    public static final String EMAIL = "fulano@email.com";
    public static final String PASS = "Seph!r0t";
    public static final String ENCODE_PASS = "1!2@3#4$_";

    public static User createUser(Long id) {
        User user = new User();

        user.setId(id);
        user.setEnabled(true);
        user.setRole(Role.ADMIN);
        user.setUsername(USERNAME);
        user.setPassword(ENCODE_PASS);
        user.setEmail(EMAIL);
        user.setCreatedAt(LocalDateTime.now());

        return user;
    }
}
