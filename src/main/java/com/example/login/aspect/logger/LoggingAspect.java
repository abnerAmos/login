package com.example.login.aspect.logger;

import com.example.login.dto.request.AuthUser;
import com.example.login.model.collection.AuditLog;
import com.example.login.repository.mongo.AuditLogRepository;
import com.example.login.security.AuthAuditorAware;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class LoggingAspect {

    private final HttpServletRequest request;
    private final AuthAuditorAware authAuditorAware;
    private final AuditLogRepository auditLogRepository;

    @Around("@annotation(org.springframework.web.bind.annotation.RequestMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.GetMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PatchMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    public Object logAndAudit(ProceedingJoinPoint joinPoint) throws Throwable {
        LocalDateTime startTime = LocalDateTime.now();

        String className = joinPoint.getTarget().getClass().getName();
        String methodName = joinPoint.getSignature().getName();
        Object[] parameters = joinPoint.getArgs();
        String ipAddress = request.getRemoteAddr();

        AuthUser authUser = null;
        try {
            authUser = authAuditorAware.getAuthUser();
        } catch (IllegalStateException e) {
            log.warn(e.getMessage());
            saveAuditLog("WARN", className, methodName, e.getMessage(), parameters, e, startTime, null, null, ipAddress);
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
            log.info(returnMessage);

            String resultMessage = initMessage + " ---> " + returnMessage;
            saveAuditLog("INFO", className, methodName, resultMessage, parameters, null, startTime, userId, userRole, ipAddress);
        } catch (Exception e) {
            String errorMessage = String.format(
                    "Método %s.%s lançou exceção: %s (Executado em %d ms)",
                    className, methodName, e.getMessage(), executionTime(startTime, LocalDateTime.now()));

            log.error(errorMessage);
            saveAuditLog("ERROR", className, methodName, errorMessage, parameters, e, startTime, userId, userRole, ipAddress);
            throw e;
        }

        return result;
    }

    private void saveAuditLog(String level, String className, String methodName, String details, Object[] parameters,
                              Exception e, LocalDateTime startTime, Long userId, String userRole, String ipAddress) {

        LocalDateTime endTime = LocalDateTime.now();

        AuditLog auditLog = AuditLog.builder()
                .level(level)
                .className(className)
                .methodName(methodName)
                .details(details)
                .parameters(parameters)
                .exception(e != null ? Arrays.toString(e.getStackTrace()) : null)
                .userId(userId)
                .userRole(userRole)
                .ip(ipAddress)
                .startTime(startTime)
                .endTime(endTime)
                .timeExecution(executionTime(startTime, endTime))
                .build();

        auditLogRepository.save(auditLog);
    }

    private Long executionTime(LocalDateTime startTime, LocalDateTime endTime) {
        return Duration.between(startTime, endTime).toMillis();
    }

    private String extractResponseContent(Object result) {
        if (result instanceof ResponseEntity<?> responseEntity) {

            // Verificar se a resposta contém o corpo
            Object body = responseEntity.getBody();

            // Se o corpo for um MappingJacksonValue, extraímos o valor real
            if (body instanceof MappingJacksonValue jacksonValue) {
                return jacksonValue.getValue().toString();
            }

            return body.toString();
        }

        // Para outros tipos de retorno, converte diretamente para string
        return result != null ? result.toString() : "<null>";
    }
}


