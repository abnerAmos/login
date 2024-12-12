package com.example.login.service.impl;

import com.example.login.dto.request.RegisterDTO;
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
    public void registerUser(RegisterDTO registerDTO) {
        var existUser = userRepository.existsByEmail(registerDTO.email());
        if (existUser) {
            throw new BadRequestException("Usuário já existe!");
        }

        User user = new User();
        user.setUsername(registerDTO.username());
        user.setPassword(passEncoder.encode(registerDTO.password()));
        user.setEmail(registerDTO.email());

        userRepository.save(user);
    }
}
