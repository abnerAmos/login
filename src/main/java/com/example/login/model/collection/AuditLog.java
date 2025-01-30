package com.example.login.model.collection;

import com.example.login.enums.Role;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@Document(collection = "auditLogs")
public class AuditLog {

    @Id
    private String id;

    private String level; // INFO, DEBUG, WARN, ERROR
    private String className; // Classe onde o log foi gerado
    private String methodName; // Método onde o log foi gerado
    private String details; // Mensagem do log
    private Object[] parameters; // Parâmetros do método
    private String exception; // Stacktrace ou mensagem de exceção
    private Long userId; // ID do usuário
    private Set<Role> userRole; // Tipo do usuário
    private String ip; // IP do usuário
    private LocalDateTime startTime; // Inicío da execução do método
    private LocalDateTime endTime; // Fim da execução do método
    private Long timeExecution; // Tempo de execução do método
}

