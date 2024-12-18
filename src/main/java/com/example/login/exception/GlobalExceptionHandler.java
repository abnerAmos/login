package com.example.login.exception;

import com.example.login.dto.response.HttpErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.*;

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
        var error = getHttpErrorResponse(request, INTERNAL_SERVER_ERROR, ex.getMessage());
        return ResponseEntity.internalServerError().body(error);
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
        var error = getHttpErrorResponse(request, UNAUTHORIZED, "Erro de autenticação: " + ex.getMessage());
        return ResponseEntity.status(UNAUTHORIZED).body(error);
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
        var error = getHttpErrorResponse(request, BAD_REQUEST, ex.getMessage());
        return ResponseEntity.badRequest().body(error);
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
        var error = getHttpErrorResponse(request, FORBIDDEN, ex.getMessage());
        return ResponseEntity.status(FORBIDDEN).body(error);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<HttpErrorResponse> handleConstraintViolationException(ConstraintViolationException ex, HttpServletRequest request) {
        String errorMessage = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .findFirst()
                .orElse("Erro de validação");

        var error = getHttpErrorResponse(request, BAD_REQUEST, errorMessage);
        return ResponseEntity.badRequest().body(error);
    }

    private HttpErrorResponse getHttpErrorResponse(HttpServletRequest request, HttpStatus badRequest, String errorMessage) {
        return new HttpErrorResponse(
                badRequest.getReasonPhrase(),
                errorMessage,
                request.getRequestURI()
        );
    }

}
