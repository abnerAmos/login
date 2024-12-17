package com.example.login.exception;

import com.example.login.dto.response.HttpErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Classe centralizada para tratar exceções da aplicação.
 * As exceções são capturadas e manipuladas para fornecer respostas detalhadas ao cliente,
 * com status HTTP apropriados e mensagens informativas.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Trata todas as exceções genéricas da aplicação.
     *
     * @param ex A exceção genérica capturada.
     * @param request A requisição HTTP que causou a exceção.
     * @return Uma resposta HTTP com status 500 e detalhes da exceção.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<HttpErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        var internalError = HttpStatus.INTERNAL_SERVER_ERROR;
        var error = new HttpErrorResponse(
                internalError.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(internalError).body(error);
    }

    /**
     * Trata exceções de autenticação lançadas pelo Spring Security.
     *
     * @param ex A exceção de autenticação capturada.
     * @param request A requisição HTTP que causou a exceção.
     * @return Uma resposta HTTP com status 401 e detalhes da exceção.
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

    /**
     * Trata exceções de requisição inválida (400) causadas por erros como dados inválidos.
     *
     * @param ex A exceção de requisição inválida (BadRequestException) capturada.
     * @param request A requisição HTTP que causou a exceção.
     * @return Uma resposta HTTP com status 400 e detalhes da exceção.
     */
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

    /**
     * Trata exceções de acesso proibido (403), onde o usuário não tem permissão para acessar um recurso.
     *
     * @param ex A exceção de acesso proibido (ForbiddenException) capturada.
     * @param request A requisição HTTP que causou a exceção.
     * @return Uma resposta HTTP com status 403 e detalhes da exceção.
     */
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
