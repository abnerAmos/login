package com.example.login.aspect.logger;

import com.example.login.model.collection.AuditLog;
import com.example.login.repository.mongo.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogBuilder {

    private final AuditLogRepository auditLogRepository;
    private final HttpServletRequest request;

    /**
     * Loga uma mensagem de nível INFO e salva no banco.
     */
    public void info(String message) {
        log.info(message);
    }

    /**
     * Loga uma mensagem de nível INFO e salva no banco.
     */
    public void info(String className, String methodName, String message,
                     Object[] parameters, LocalDateTime startTime, Long userId, String userRole) {
        saveAuditLog("INFO", className, methodName, message, parameters, null, startTime, userId, userRole);
    }

    /**
     * Loga uma mensagem de nível WARN e salva no banco.
     */
    public void warn(String className, String methodName, String details,
                     Object[] parameters, Exception e, LocalDateTime startTime, Long userId,
                     String userRole) {
        log.warn(details, e);
        saveAuditLog("WARN", className, methodName, details, parameters, e, startTime, userId, userRole);
    }

    /**
     * Loga uma mensagem de nível ERROR e salva no banco.
     */
    public void error(String className, String methodName, String message,
                      Object[] parameters, Exception e, LocalDateTime startTime, Long userId,
                      String userRole) {
        log.error(message, e);
        saveAuditLog("ERROR", className, methodName, message, parameters, e, startTime, userId, userRole);
    }

    private void saveAuditLog(String level, String className, String methodName, String details, Object[] parameters,
                              Exception e, LocalDateTime startTime, Long userId, String userRole) {

        String ipAddress = request.getRemoteAddr();

        LocalDateTime endTime = LocalDateTime.now();
        var executionTime = Duration.between(startTime, endTime).toMillis();

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
                .timeExecution(executionTime)
                .build();

        auditLogRepository.save(auditLog);
    }
}
