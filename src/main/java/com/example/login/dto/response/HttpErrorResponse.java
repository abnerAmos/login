package com.example.login.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;

public record HttpErrorResponse(
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    Instant timestamp,
    String status,
    String messageError,
    String path) {

    public HttpErrorResponse(String status, String messageError, String path) {
        this(Instant.now(), status, messageError, path);
    }
}
