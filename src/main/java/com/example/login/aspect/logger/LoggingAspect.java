package com.example.login.aspect.logger;

import com.example.login.dto.request.AuthUser;
import com.example.login.security.AuthAuditorAware;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;

@Aspect
@Component
@RequiredArgsConstructor
public class LoggingAspect {

    private final LogBuilder log;
    private final SanitizerLogs sanitizerLogs;
    private final AuthAuditorAware authAuditorAware;

    @Around("execution(* com.example..*Controller.*(..))")
    public Object logAndAudit(ProceedingJoinPoint joinPoint) throws Throwable {
        LocalDateTime startTime = LocalDateTime.now();

        String className = joinPoint.getTarget().getClass().getName();
        String methodName = joinPoint.getSignature().getName();
        Object[] parameters = sanitizerLogs.sanitizeParameters(joinPoint.getArgs());

        AuthUser authUser = null;
        try {
            authUser = authAuditorAware.getAuthUser();
        } catch (IllegalStateException e) {
            log.warn(className, methodName, e.getMessage(), parameters, e, startTime, null, null);
        }

        Long userId = authUser != null ? authUser.id() : null;
        String userRole = authUser != null? authUser.role() : null;

        Object result;
        try {
            String initMessage = String.format(
                    "Iniciando método: %s.%s com parâmetros: %s",
                    className, methodName, Arrays.toString(parameters));
            log.info(initMessage);

            result = joinPoint.proceed();

            String returnMessage = String.format(
                    "Método %s.%s retornou: %s (Executado em %d ms)",
                    className, methodName, extractResponseContent(result), executionTime(startTime, LocalDateTime.now()));

            log.info(className, methodName, returnMessage, parameters, startTime, userId, userRole);
        } catch (Exception e) {
            String errorMessage = String.format(
                    "Método %s.%s lançou exceção: %s (Executado em %d ms)",
                    className, methodName, e.getMessage(), executionTime(startTime, LocalDateTime.now()));

            log.error(className, methodName, errorMessage, parameters, e, startTime, userId, userRole);
            throw e;
        }

        return result;
    }

    private String extractResponseContent(Object result) {
        if (result instanceof ResponseEntity<?> responseEntity) {
            Object body = responseEntity.getBody();
            return sanitizerLogs.sanitizeObjectForLogging(body).toString();
        }

        return result != null
                ? sanitizerLogs.sanitizeObjectForLogging(result).toString()
                : "<null>";
    }

    private Long executionTime(LocalDateTime startTime, LocalDateTime endTime) {
        return Duration.between(startTime, endTime).toMillis();
    }

}
