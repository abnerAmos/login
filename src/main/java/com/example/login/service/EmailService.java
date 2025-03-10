package com.example.login.service;

public interface EmailService {

    void sendRegisterEmail(String receiverEmail);

    void validationCode(String email, String code);

    void sendRefreshCode(String email);

    void sendValidationEmail(String email, String subject, String text);
}
