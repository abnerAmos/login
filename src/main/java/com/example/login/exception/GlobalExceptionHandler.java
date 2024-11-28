package com.example.login.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

/**
 * Classe centralizada para tratar exceções da aplicação, incluindo exceções de autenticação.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Trata exceções de autenticação lançadas pelo Spring Security.
     *
     * @param ex A exceção de autenticação capturada.
     * @return Uma resposta HTTP com status 401 (Unauthorized).
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<BodyHttpException> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        var error = new BodyHttpException(
                Instant.now(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                "Erro de autenticação: " + ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(error);
    }
}
