package com.example.login.service.impl;

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
    public void registerUser(User user) {
        var existUser = userRepository.existsByEmail(user.getEmail());
        if (existUser) {
            throw new BadRequestException("Usuário já existe!");
        }

        User newUser = new User();
        newUser.setEmail(user.getEmail());
        newUser.setPassword(passEncoder.encode(user.getPassword()));

        userRepository.save(user);
    }

    @Override
    public User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Usuário já existe!"));
    }
}
