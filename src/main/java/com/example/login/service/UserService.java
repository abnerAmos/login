package com.example.login.service;

import com.example.login.dto.request.UserRequest;
import com.example.login.model.User;

public interface UserService {

    void registerUser(UserRequest user);

    User findUser(Long id);
}
