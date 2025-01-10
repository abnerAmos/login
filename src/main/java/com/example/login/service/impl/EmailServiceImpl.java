package com.example.login.service.impl;

import com.example.login.cache.ValidationCodeCache;
import com.example.login.exception.BadRequestException;
import com.example.login.exception.InternalServerErrorException;
import com.example.login.repository.UserRepository;
import com.example.login.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    private final UserRepository userRepository;

    private final ValidationCodeCache validationCodeCache;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Override
    public void sendValidationEmail(String receiverEmail, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(senderEmail);
            helper.setTo(receiverEmail);
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

        String cachedCode = validationCodeCache.getValidationCode(email);
        if (!code.equals(cachedCode)) {
            throw new BadRequestException("Código de validação expirado ou inválido.");
        }

        validationCodeCache.invalidateValidationCode(email);

        user.setEnabled(true);
        userRepository.save(user);
    }

    @Cacheable(value = "validationCodes", key = "#email")
    public String generateValidationCode(String email) {
        String character = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        int codeLength = 6;

        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < codeLength; i++) {
            code.append(character.charAt(random.nextInt(character.length())));
        }
        return code.toString();
    }

}
