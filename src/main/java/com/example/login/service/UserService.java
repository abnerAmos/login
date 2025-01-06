package com.example.login.service;

import com.example.login.dto.request.NewUserRequest;
import com.example.login.model.User;

public interface UserService {

    void registerUser(NewUserRequest user);

    User findUser(Long id);
}
