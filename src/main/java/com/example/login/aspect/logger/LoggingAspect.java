package com.example.login.aspect.logger;

import com.example.login.model.collection.LogEntry;
import com.example.login.repository.mongo.LogRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;

@Aspect
@Component
@RequiredArgsConstructor
public class LoggingAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingAspect.class);
    private final LogRepository logRepository;

    // Define o ponto de corte para os métodos desejados
    @Pointcut("execution(* com.example..*Controller.*(..))")
    public void applicationMethods() {
    }

    @Around("applicationMethods()")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        String className = joinPoint.getTarget().getClass().getName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        try {
            String entryMessage = String.format("Iniciando método: %s.%s com parâmetros: %s", className, methodName, Arrays.toString(args));
            LOGGER.info(entryMessage);
            buildLogEntry("INFO", className, methodName, entryMessage, args, null, startTime, null, null);

            Object result = joinPoint.proceed();
            long endExecutionTime = System.currentTimeMillis();
            long executionTime = endExecutionTime - startTime;

            String exitMessage = String.format("Método %s.%s retornou: %s (Executado em %d ms)", className, methodName, result, executionTime);
            LOGGER.info(exitMessage);
            buildLogEntry("INFO", className, methodName, exitMessage, args, null, startTime, endExecutionTime, executionTime);

            return result;
        } catch (Exception e) {
            long endExecutionTime = System.currentTimeMillis();
            long executionTime = endExecutionTime - startTime;

            String errorMessage = String.format("Método %s.%s lançou exceção: %s (Executado em %d ms)", className, methodName, e.getMessage(), executionTime);
            LOGGER.error(errorMessage, e);

            buildLogEntry("ERROR", className, methodName, errorMessage, args, e.getMessage(), startTime, endExecutionTime, executionTime);

            throw e;
        }
    }

    private void buildLogEntry(String level, String className, String methodName, String details, Object[] parameters,
                               String exception, Long startTime, Long endTime, Long executionTime) {
        LogEntry log = LogEntry.builder()
                .timestamp(LocalDateTime.now())
                .level(level)
                .className(className)
                .methodName(methodName)
                .details(details)
                .parameters(parameters)
                .exception(exception)
                .userId(getCurrentUserId()) // Método para obter o usuário logado
                .userRole(getCurrentUserRole()) // Método para obter o tipo do usuário
                .ip(getUserIp()) // Método para obter o IP do usuário
                .startTime(toLocalDateTime(startTime))
                .endTime(toLocalDateTime(endTime))
                .timeExecution(executionTime)
                .build();

        logRepository.save(log);
    }

    private LocalDateTime toLocalDateTime(Long millis) {
        return millis != null
                ? LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault())
                : null;
    }

    private String getCurrentUserId() {
        // Implementar lógica para obter o ID do usuário logado
        return "anonymous";
    }

    private String getCurrentUserRole() {
        // Implementar lógica para obter o tipo do usuário
        return "anonymous";
    }

    private String getUserIp() {
        // Implementar lógica para capturar o IP do usuário
        return "127.0.0.1";
    }
}

