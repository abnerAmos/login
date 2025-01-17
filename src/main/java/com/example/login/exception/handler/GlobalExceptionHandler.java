package com.example.login.exception.handler;

import com.example.login.dto.response.HttpErrorResponse;
import com.example.login.exception.BadRequestException;
import com.example.login.exception.ForbiddenException;
import com.example.login.exception.InternalServerErrorException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
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
     * Trata exceções de erro interno.
     *
     * @param ex A exceção capturada.
     * @param request A requisição HTTP que causou a exceção.
     * @return Uma resposta HTTP com status 500 e detalhes da exceção.
     */
    @ExceptionHandler(InternalServerErrorException.class)
    public ResponseEntity<HttpErrorResponse> handleInternalServerErrorException(InternalServerErrorException ex, HttpServletRequest request) {
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

    /**
     * Trata exceções de violação de restrições de validação.
     * Este método é responsável por capturar exceções de validação em campos específicos das entidades.
     *
     * @param ex A exceção de violação de restrição (ConstraintViolationException) capturada.
     * @param request A requisição HTTP que causou a exceção.
     * @return Uma resposta HTTP com status 400 e detalhes da exceção.
     * A resposta será formatada para informar a violação das regras de validação.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<HttpErrorResponse> handleConstraintViolationException(ConstraintViolationException ex, HttpServletRequest request) {
        String errorMessage = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .findFirst()
                .orElse("Erro de validação");

        var error = getHttpErrorResponse(request, BAD_REQUEST, errorMessage);
        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Trata exceções de validação de argumentos dos métodos.
     * Este método é utilizado para capturar exceções de campos de entrada inválidos,
     * como em requisições JSON que não atendem aos critérios de validação definidos.
     *
     * @param ex A exceção de argumento não válido (MethodArgumentNotValidException) capturada.
     * @param request A requisição HTTP que causou a exceção.
     * @return Uma resposta HTTP com status 400 e detalhes da exceção.
     * A resposta será formatada para informar quais campos falharam na validação.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<HttpErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        // Extrair todas as mensagens de erro
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .findFirst()
                .orElse("Erro de validação");

        var error = getHttpErrorResponse(request, BAD_REQUEST, errorMessage);
        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Método auxiliar para criar a estrutura da resposta de erro.
     *
     * @param request A requisição HTTP original.
     * @param badRequest O status HTTP da resposta de erro.
     * @param errorMessage A mensagem detalhada do erro.
     * @return Um objeto HttpErrorResponse com as informações formatadas.
     */
    private HttpErrorResponse getHttpErrorResponse(HttpServletRequest request, HttpStatus badRequest, String errorMessage) {
        return new HttpErrorResponse(
                badRequest.getReasonPhrase(),
                errorMessage,
                request.getRequestURI()
        );
    }

}
