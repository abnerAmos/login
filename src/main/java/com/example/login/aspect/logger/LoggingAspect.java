package com.example.login.aspect.logger;

import com.example.login.dto.request.AuthUser;
import com.example.login.dto.response.LogContextResponse;
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
import java.util.Arrays;
import java.util.Optional;

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
        Role userRole = authUser.map(AuthUser::role).orElse(null);

        LogContextResponse logContext = new LogContextResponse(
                className, methodName, startTime, parameters, userId, userRole);

        Object result;
        try {
            logInitMessage(logContext);

            result = joinPoint.proceed();

            logReturnMessage(logContext, result);
        } catch (Exception e) {
            logErrorMessage(logContext, e);
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
     * Constrói uma mensagem informativa de inicio de chamada para o log.
     */
    private void logInitMessage(LogContextResponse logContext) {
        String initMessage = String.format(
                "Iniciando método: %s.%s com parâmetros: %s",
                logContext.className(), logContext.methodName(), Arrays.toString(logContext.parameters()));

        log.info(logContext.className(), logContext.methodName(), initMessage,
                logContext.parameters(), logContext.startTime(), logContext.userId(), logContext.role());
    }

    /**
     * Constrói uma mensagem informativa de retorno para o log.
     */
    private void logReturnMessage(LogContextResponse logContext, Object result) {
        String returnMessage = String.format(
                "Método %s.%s retornou: %s (Executado em %d ms)",
                logContext.className(), logContext.methodName(),
                extractResponseContent(result), executionTime(logContext.startTime(), LocalDateTime.now()));

        log.info(logContext.className(), logContext.methodName(), returnMessage, logContext.parameters(),
                logContext.startTime(), logContext.userId(), logContext.role());
    }

    /**
     * Registra uma mensagem de erro no log quando o método lança uma exceção.
     */
    private void logErrorMessage(LogContextResponse logContext, Exception e) {
        String errorMessage = String.format(
                "Método %s.%s lançou exceção: %s (Executado em %d ms)",
                logContext.className(), logContext.methodName(), e.getMessage(),
                executionTime(logContext.startTime(), LocalDateTime.now()));

        log.error(logContext.className(), logContext.methodName(), errorMessage, e,
                logContext.startTime(), logContext.userId(), logContext.role());
    }

    /**
     * Extrai e sanitiza o conteúdo de uma resposta para fins de logging.
     *
     * @param result O objeto de resposta a ser processado, podendo ser uma instância de {@link ResponseEntity}
     *               ou qualquer outro tipo de objeto retornado pelo controlador.
     * @return Uma representação em string do conteúdo da resposta, sanitizada para logging.
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
