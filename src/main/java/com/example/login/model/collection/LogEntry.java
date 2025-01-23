package com.example.login.model.collection;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@Document(collection = "logs")
public class LogEntry {

    @Id
    private String id;

    private LocalDateTime timestamp; // Data e hora do evento
    private String level; // INFO, DEBUG, WARN, ERROR
    private String className; // Classe onde o log foi gerado
    private String methodName; // Método onde o log foi gerado
    private String details; // Mensagem do log
    private Object[] parameters; // Parâmetros do método
    private String exception; // Stacktrace ou mensagem de exceção
    private String userId; // ID do usuário
    private String userRole; // Tipo do usuário
    private String ip; // IP do usuário
    private LocalDateTime startTime; // Inicío da execução do método
    private LocalDateTime endTime; // Fim da execução do método
    private Long timeExecution; // Tempo de execução do método
}

