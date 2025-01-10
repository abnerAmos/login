package com.example.login.service;

public interface EmailService {

    void sendValidationEmail(String receiverEmail, String code);

    void validationCode(String email, String code);

    String generateValidationCode(String email);
}
