package com.example.login.service.impl;

import com.example.login.dto.request.UserRequest;
import com.example.login.enums.Role;
import com.example.login.exception.BadRequestException;
import com.example.login.model.User;
import com.example.login.repository.UserRepository;
import com.example.login.security.AuthAuditorAware;
import com.example.login.service.EmailService;
import com.example.login.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passEncoder;
    private final EmailService emailService;
    private final AuthAuditorAware authAuditorAware;

    /**
     * Registra um novo usuário no sistema e envia um e-mail de validação.
     *
     * @param user Um objeto 'UserRequest' contendo as informações do novo usuário, como e-mail e senha.
     * @throws BadRequestException Se já existir um usuário com o mesmo e-mail cadastrado.
     */
    @Override
    @Transactional
    public void registerUser(UserRequest user) {
        var existUser = userRepository.existsByEmail(user.email());
        if (existUser) {
            throw new BadRequestException("Usuário já existe!");
        }

        Role role = Role.getRole(user.role());

        User newUser = new User();
        newUser.setEmail(user.email());
        newUser.setPassword(passEncoder.encode(user.password()));
        newUser.setRole(role);
        newUser.setEnabled(user.isExperimental());

        userRepository.save(newUser);

        if (!user.isExperimental()) {
            emailService.sendRegisterEmail(user.email());
        }
    }

    @Override
    public User findUser() {
        var userId = authAuditorAware.getAuthUser().id();

        return userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("Usuário inexistente!"));
    }

}
