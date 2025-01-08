package com.example.login.service;

public interface EmailService {

    void sendValidationEmail(String to, String code);

    void validationCode(String email, String code);
}
