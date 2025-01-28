package com.example.login.aspect.logger;

import com.example.login.dto.request.AuthUser;
import com.example.login.model.collection.AuditLog;
import com.example.login.repository.mongo.AuditLogRepository;
import com.example.login.security.AuthAuditorAware;
import com.example.login.util.Sensitive;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Aspect
@Slf4j
@Component
@RequiredArgsConstructor
public class LoggingAspect {

    private final HttpServletRequest request;
    private final AuthAuditorAware authAuditorAware;
    private final AuditLogRepository auditLogRepository;

    @Around("execution(* com.example..*Controller.*(..))")
    public Object logAndAudit(ProceedingJoinPoint joinPoint) throws Throwable {
        LocalDateTime startTime = LocalDateTime.now();

        String className = joinPoint.getTarget().getClass().getName();
        String methodName = joinPoint.getSignature().getName();
        Object[] parameters = sanitizeParameters(joinPoint.getArgs());

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
            Object body = responseEntity.getBody();
            return sanitizeObjectForLogging(body).toString();
        }

        return result != null ? sanitizeObjectForLogging(result).toString() : "<null>";
    }

    private Object[] sanitizeParameters(Object[] parameters) {
        return Arrays.stream(parameters)
                .map(this::sanitizeObjectForLogging)
                .toArray();
    }

    private Object sanitizeObjectForLogging(Object obj) {
        if (obj == null) return null;

        // Ignorar sanitização para tipos simples e classes de pacotes do Java
        if (obj.getClass().isPrimitive() || obj instanceof String || obj instanceof Number ||
                obj instanceof Boolean || obj.getClass().getPackageName().startsWith("java.")) {
            return obj;
        }

        if (obj instanceof Map<?, ?> map) {
            return map.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> sanitizeObjectForLogging(entry.getValue())));
        }

        if (obj instanceof Collection<?> collection) {
            return collection.stream()
                    .map(this::sanitizeObjectForLogging)
                    .collect(Collectors.toList());
        }

        return sanitizeComplexObject(obj);
    }

    private Object sanitizeComplexObject(Object obj) {
        if (obj == null) return null;

        // Ignorar objetos de pacotes padrão do Java ou classes imutáveis
        if (obj.getClass().getPackageName().startsWith("java.")) {
            return obj; // Retorna o objeto sem alterar
        }

        try {
            // Cria um mapa representando os campos do objeto
            Map<String, Object> sanitizedFields = new HashMap<>();
            Field[] fields = obj.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                Object fieldValue = field.get(obj);

                // Mascarar valores sensíveis
                if (field.isAnnotationPresent(Sensitive.class)) {
                    sanitizedFields.put(field.getName(), "******");
                } else {
                    sanitizedFields.put(field.getName(), sanitizeObjectForLogging(fieldValue));
                }
            }
            return sanitizedFields; // Retorna o mapa como representação segura
        } catch (Exception e) {
            log.warn("Falha ao sanitizar objeto para log: {}", e.getMessage());
            return obj.toString(); // Retorna a string do objeto em caso de falha
        }
    }

}
