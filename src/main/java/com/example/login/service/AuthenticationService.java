package com.example.login.service;

import com.example.login.dto.request.AlterPassRequest;

public interface AuthenticationService {

    void forgotPassword(String email);

    void resetPassword(AlterPassRequest alterPassRequest);
}
