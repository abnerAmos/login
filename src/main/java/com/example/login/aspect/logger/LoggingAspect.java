package com.example.login.aspect.logger;

import com.example.login.dto.request.AuthUser;
import com.example.login.enums.Role;
import com.example.login.security.AuthAuditorAware;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

/**
 * Aspecto responsável por registrar logs de chamadas a métodos de controllers.
 * Intercepta métodos dentro de classes que terminam com 'Controller' para capturar e registrar informações sobre
 * a execução, incluindo tempo de execução, parâmetros, resultados e possíveis exceções.
 */
@Aspect
@Component
@RequiredArgsConstructor
public class LoggingAspect {

    private final LogBuilder log;
    private final SanitizerLogs sanitizerLogs;
    private final AuthAuditorAware authAuditorAware;

    /**
     * Intercepta métodos de qualquer classe Controller e realiza logging e auditoria.
     *
     * @param joinPoint O ponto de junção da execução do método interceptado.
     * @return O resultado da execução do método interceptado.
     * @throws Throwable Qualquer exceção lançada pelo método interceptado.
     */
    @Around("execution(* com.example..*Controller.*(..))")
    public Object logAndAudit(ProceedingJoinPoint joinPoint) throws Throwable {
        LocalDateTime startTime = LocalDateTime.now();
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = joinPoint.getSignature().getName();
        Object[] parameters = sanitizerLogs.sanitizeParameters(joinPoint.getArgs());

        // Obtém informações do usuário autenticado de forma segura
        Optional<AuthUser> authUser = getAuthenticatedUser(className, methodName, parameters, startTime);
        Long userId = authUser.map(AuthUser::id).orElse(null);
        Set<Role> userRoles = authUser.map(AuthUser::roles).orElse(null);

        Object result;
        try {
            log.info(String.format("Iniciando método: %s.%s com parâmetros: %s",
                    className, methodName, sanitizerLogs.sanitizeObjectForLogging(parameters)));

            result = joinPoint.proceed();

            logSuccessMessage(className, methodName, result, startTime, parameters, userId, userRoles);
        } catch (Exception e) {
            logErrorMessage(className, methodName, e, startTime, parameters, userId, userRoles);
            throw e;
        }

        return result;
    }

    /**
     * Obtém o usuário autenticado atual de forma segura.
     * Caso ocorra um erro ao obter as informações do usuário, um aviso será registrado no log.
     *
     * @param className  Nome da classe onde a autenticação está sendo obtida.
     * @param methodName Nome do método onde a autenticação está sendo obtida.
     * @param parameters Parâmetros do método interceptado.
     * @param startTime  Tempo de início da execução do método.
     * @return Um Optional contendo o usuário autenticado, caso disponível.
     */
    private Optional<AuthUser> getAuthenticatedUser(String className, String methodName, Object[] parameters, LocalDateTime startTime) {
        try {
            return Optional.ofNullable(authAuditorAware.getAuthUser());
        } catch (IllegalStateException e) {
            log.warn(className, methodName, e.getMessage(), parameters, e, startTime, null, null);
            return Optional.empty();
        }
    }

    /**
     * Constrói uma mensagem de sucesso para o log.
     */
    private void logSuccessMessage(String className, String methodName, Object result, LocalDateTime startTime,
                                   Object[] parameters, Long userId, Set<Role> roles) {
        String returnMessage = String.format(
                "Método %s.%s retornou: %s (Executado em %d ms)",
                className, methodName, extractResponseContent(result), executionTime(startTime, LocalDateTime.now()));

        log.info(className, methodName, returnMessage, parameters, startTime, userId, roles);
    }

    /**
     * Registra uma mensagem de sucesso no log após a execução bem-sucedida do método.
     */
    private void logErrorMessage(String className, String methodName, Exception e, LocalDateTime startTime,
                                 Object[] parameters, Long userId, Set<Role> roles) {
        String errorMessage = String.format(
                "Método %s.%s lançou exceção: %s (Executado em %d ms)",
                className, methodName, e.getMessage(), executionTime(startTime, LocalDateTime.now()));

        log.error(className, methodName, errorMessage, parameters, e, startTime, userId, roles);
    }

    /**
     * Registra uma mensagem de erro no log quando o método lança uma exceção.
     */
    private String extractResponseContent(Object result) {
        if (result instanceof ResponseEntity<?> responseEntity) {
            Object body = responseEntity.getBody();
            return sanitizerLogs.sanitizeObjectForLogging(body).toString();
        }

        return result != null ? sanitizerLogs.sanitizeObjectForLogging(result).toString() : "<null>";
    }

    /**
     * Calcula o tempo de execução em milissegundos.
     */
    private Long executionTime(LocalDateTime startTime, LocalDateTime endTime) {
        return Duration.between(startTime, endTime).toMillis();
    }

}
