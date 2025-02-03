package com.example.login.aspect.logger;

import com.example.login.enums.Role;
import com.example.login.model.collection.AuditLog;
import com.example.login.repository.mongo.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogBuilder {

    private final AuditLogRepository auditLogRepository;
    private final HttpServletRequest request;

    /**
     * Loga uma mensagem de nível INFO.
     */
    public void info(String message) {
        log.info(message);
    }

    /**
     * Loga uma mensagem de nível WARN.
     */
    public void warn(String message, Exception e) {
        log.warn(message, e);
    }

    /**
     * Loga uma mensagem de nível INFO e salva no banco.
     */
    public void info(String className, String methodName, String message,
                     Object[] parameters, LocalDateTime startTime, Long userId, Set<Role> userRole) {
        log.info(message);
        saveAuditLog("INFO", className, methodName, message, parameters, null, startTime, userId, userRole);
    }

    /**
     * Loga uma mensagem de nível WARN e salva no banco.
     */
    public void warn(String className, String methodName, String details,
                     Object[] parameters, Exception e, LocalDateTime startTime, Long userId,
                     Set<Role> userRole) {
        log.warn(details, e);
        saveAuditLog("WARN", className, methodName, details, parameters, e, startTime, userId, userRole);
    }

    /**
     * Loga uma mensagem de nível ERROR e salva no banco.
     */
    public void error(String className, String methodName, String message,
                      Exception e, LocalDateTime startTime, Long userId, Set<Role> userRole) {
        log.error(message, e);
        saveAuditLog("ERROR", className, methodName, message, null, e, startTime, userId, userRole);
    }

    /**
     * Registra um log de auditoria no banco de dados.
     *
     * <p>Este método cria e salva um registro de auditoria contendo informações sobre a execução de um método,
     * como classe, nome do método, detalhes, parâmetros, exceções e tempo de execução. Também armazena
     * informações do usuário autenticado e seu endereço IP.</p>
     *
     * @param level        O nível do log (ex: "INFO", "ERROR", "DEBUG").
     * @param className    O nome da classe onde o log foi gerado.
     * @param methodName   O nome do método onde o log foi gerado.
     * @param details      Informações adicionais sobre a ação executada.
     * @param parameters   Parâmetros fornecidos ao método registrado no log.
     * @param e            Exceção lançada durante a execução, se houver (caso contrário, será null).
     * @param startTime    Momento em que o método começou a ser executado.
     * @param userId       Identificação do usuário que executou a ação.
     * @param userRole     Conjunto de papéis (roles) do usuário autenticado.
     */
    private void saveAuditLog(String level, String className, String methodName, String details, Object[] parameters,
                              Exception e, LocalDateTime startTime, Long userId, Set<Role> userRole) {

        String ipAddress = request.getRemoteAddr();

        LocalDateTime endTime = LocalDateTime.now();
        var executionTime = Duration.between(startTime, endTime).toMillis();

        AuditLog auditLog = AuditLog.builder()
                .level(level)
                .className(className)
                .methodName(methodName)
                .details(details)
                .parameters(parameters)
                .exception(getException(e))
                .userId(userId)
                .userRole(userRole)
                .ip(ipAddress)
                .startTime(startTime)
                .endTime(endTime)
                .timeExecution(executionTime)
                .build();

        auditLogRepository.save(auditLog);
    }

    /**
     * Obtém uma representação simplificada da exceção, limitando o número de linhas do stack trace.
     * <p>
     * Este método recebe uma exceção e extrai até 5 linhas do stack trace para evitar logs excessivamente
     * longos e manter a legibilidade. Se a exceção for {@code null}, retorna {@code null}.
     *
     * @param e A exceção da qual será extraído o stack trace.
     * @return Uma string contendo até 5 linhas do stack trace da exceção ou {@code null} se a exceção for {@code null}.
     */
    private String getException(Exception e) {
        String exceptionMessage = null;
        if (e != null) {
            int maxLines = 5;
            exceptionMessage = Arrays.stream(e.getStackTrace())
                    .limit(maxLines)
                    .map(StackTraceElement::toString)
                    .collect(Collectors.joining("\n"));
        }
        return exceptionMessage;
    }
}
