package com.example.login.service.impl;

import com.example.login.exception.BadRequestException;
import com.example.login.exception.InternalServerErrorException;
import com.example.login.repository.UserRepository;
import com.example.login.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    private final UserRepository userRepository;

    @Value("${spring.mail.username}")
    private String sender;

    @Override
    public void sendValidationEmail(String receiver, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(sender);
            helper.setTo(receiver);
            helper.setSubject("Código de Validação");
            helper.setText("Seu código de validação é: <b>" + code + "</b>", true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new InternalServerErrorException("Erro durante o envio de e-mail");
        }
    }

    @Override
    public void validationCode(String email, String code) {
        var user = userRepository.findByEmail(email);
        if (user == null) {
            throw new BadRequestException("Usuário não encontrado!");
        }

        if (user.getValidationCode() == null || !user.getValidationCode().equals(code.toLowerCase())) {
            throw new BadRequestException("Código de validação inválido.");
        }

        if (user.getValidationCodeExpiry().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Código de validação expirado.");
        }

        user.setEnabled(true);
        user.setValidationCode(null);
        user.setValidationCodeExpiry(null);

        userRepository.save(user);
    }

}
