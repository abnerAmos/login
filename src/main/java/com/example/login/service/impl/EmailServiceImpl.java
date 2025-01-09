package com.example.login.service.impl;

import com.example.login.exception.BadRequestException;
import com.example.login.exception.InternalServerErrorException;
import com.example.login.repository.UserRepository;
import com.example.login.service.EmailService;
import com.example.login.service.ValidationCodeService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    private final UserRepository userRepository;

    private final ValidationCodeService validationCodeService;

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

        String cachedCode = validationCodeService.getValidationCode(email);
        if (code.equals(cachedCode)) {
            throw new BadRequestException("Código de validação inválido.");
        }

//        if (user.getValidationCodeExpiry().isBefore(LocalDateTime.now())) {
//            throw new BadRequestException("Código de validação expirado.");
//        }

        validationCodeService.invalidateValidationCode(email);

        user.setEnabled(true);
        userRepository.save(user);
    }

}
