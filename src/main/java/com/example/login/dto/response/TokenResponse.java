package com.example.login.dto.response;

import com.example.login.view.Views;
import com.fasterxml.jackson.annotation.JsonView;

@JsonView(Views.Basic.class)
public record TokenResponse(String token) {
}
