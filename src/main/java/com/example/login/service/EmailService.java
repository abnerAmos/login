package com.example.login.service;

import com.example.login.dto.request.CodeRequest;

public interface EmailService {

    void sendRegisterEmail(String receiverEmail);

    void validationCode(CodeRequest codeRequest);

    void sendRefreshCode(String email);

    void sendValidationEmail(String email, String subject, String text);
}
