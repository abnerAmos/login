package com.example.login.dto.request;

import com.example.login.validation.ValidPassword;

public record AlterPassRequest(String code, @ValidPassword String password) {
}
