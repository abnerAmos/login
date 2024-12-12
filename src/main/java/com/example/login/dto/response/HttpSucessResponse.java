package com.example.login.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.http.HttpStatus;

import java.time.Instant;

public record HttpSucessResponse(
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    Instant timestamp,
    HttpStatus status,
    String message) {

    public HttpSucessResponse(HttpStatus status, String message) {
        this(Instant.now(), status, message);
    }

}
