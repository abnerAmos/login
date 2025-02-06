package com.example.login.dto.response;

import com.example.login.aspect.view.Views;
import com.example.login.util.Sensitive;
import com.fasterxml.jackson.annotation.JsonView;

@JsonView(Views.Basic.class)
public record TokenResponse(
        @Sensitive String token,
        @Sensitive String refreshToken) {
}
