package com.example.login.dto.response;

import com.example.login.aspect.view.Views;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.http.HttpStatus;

import java.time.Instant;

@JsonView(Views.Basic.class)
public record HttpSuccessResponse(
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    Instant timestamp,
    HttpStatus status,
    String message) {

    public HttpSuccessResponse(HttpStatus status, String message) {
        this(Instant.now(), status, message);
    }

    public HttpSuccessResponse(String message) {
        this(Instant.now(), HttpStatus.OK, message);
    }

}
