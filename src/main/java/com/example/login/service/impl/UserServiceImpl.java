package com.example.login.service.impl;

import com.example.login.dto.request.NewUserRequest;
import com.example.login.exception.BadRequestException;
import com.example.login.model.User;
import com.example.login.repository.UserRepository;
import com.example.login.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passEncoder;

    @Override
    public void registerUser(NewUserRequest user) {
        var existUser = userRepository.existsByEmail(user.email());
        if (existUser) {
            throw new BadRequestException("Usuário já existe!");
        }

        User newUser = new User();
        newUser.setEmail(user.email());
        newUser.setPassword(passEncoder.encode(user.password()));

        userRepository.save(newUser);
    }

    @Override
    public User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Usuário já existe!"));
    }
}
