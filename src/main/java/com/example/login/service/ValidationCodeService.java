package com.example.login.service;

public interface ValidationCodeService {

    String generateValidationCode(String email);

    void invalidateValidationCode(String email);

    String getValidationCode(String email);
}
