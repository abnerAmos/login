package com.example.login.exception;

import com.example.login.dto.response.HttpErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
    public ResponseEntity<HttpErrorResponse> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        var unauthorizedException = HttpStatus.UNAUTHORIZED;
        var error = new HttpErrorResponse(
                unauthorizedException.getReasonPhrase(),
                "Erro de autenticação: " + ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(unauthorizedException).body(error);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<HttpErrorResponse> handleBadRequestException(BadRequestException ex, HttpServletRequest request) {
        var badRequestException = HttpStatus.BAD_REQUEST;
        var error = new HttpErrorResponse(
                badRequestException.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(badRequestException).body(error);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<HttpErrorResponse> handleBadRequestException(ForbiddenException ex, HttpServletRequest request) {
        var forbiddenException = HttpStatus.FORBIDDEN;
        var error = new HttpErrorResponse(
                forbiddenException.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(forbiddenException).body(error);
    }
}
