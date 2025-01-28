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

        AuthUser authUser = null;
        try {
            authUser = authAuditorAware.getAuthUser();
        } catch (IllegalStateException e) {
            log.warn(e.getMessage());
//            throw new IllegalStateException(e.getMessage());
        }

        Long userId = authUser != null ? authUser.id() : null;
        String userRole = authUser != null? authUser.role() : null;
        String ipAddress = request.getRemoteAddr();

        Object result;
        try {
            // Executa o método original
            result = joinPoint.proceed();

            // Log INFO no sucesso
            log.info("Executed {}#{} with parameters: {}", className, methodName, parameters);
        } catch (Exception e) {
            // Log ERROR no caso de exceção
            log.error("Error in {}#{}: {}", className, methodName, e.getMessage());
            saveAuditLog(className, methodName, parameters, e, startTime, userId, userRole, ipAddress);
            throw e;
        }

        // Salva auditoria ao final do método
        saveAuditLog(className, methodName, parameters, null, startTime, userId, userRole, ipAddress);
        return result;
    }

    private void saveAuditLog(String className, String methodName, Object[] parameters, Exception e,
                              LocalDateTime startTime, Long userId, String userRole, String ipAddress) {
        LocalDateTime endTime = LocalDateTime.now();
        long executionTime = Duration.between(startTime, endTime).toMillis();

        AuditLog auditLog = AuditLog.builder()
                .timestamp(endTime)
                .level(e == null ? "INFO" : "ERROR")
                .className(className)
                .methodName(methodName)
                .details(e == null ? "Execução bem-sucedida" : e.getMessage())
                .parameters(parameters)
                .exception(e != null ? Arrays.toString(e.getStackTrace()) : null)
                .userId(userId)
                .userRole(userRole)
                .ip(ipAddress)
                .startTime(startTime)
                .endTime(endTime)
                .timeExecution(executionTime)
                .build();

        auditLogRepository.save(auditLog);
    }
}


