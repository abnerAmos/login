package com.example.login.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;
import java.time.ZonedDateTime;

public record HttpErrorResponse(
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "America/Sao_Paulo")
    Instant timestamp,
    String status,
    String messageError,
    String path) {

    public HttpErrorResponse(String status, String messageError, String path) {
        this(
                ZonedDateTime.now().toInstant(),
                status,
                messageError,
                path
        );
    }
}
