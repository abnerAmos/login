package com.example.login.aspect.logger;

import com.example.login.util.Sensitive;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SanitizerLogs {

    private final LogBuilder log;

    public Object[] sanitizeParameters(Object[] parameters) {
        return Arrays.stream(parameters)
                .map(this::sanitizeObjectForLogging)
                .toArray();
    }

    public Object sanitizeObjectForLogging(Object obj) {
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
            log.warn("Falha ao sanitizar objeto para log: {}", e);
            return obj.toString(); // Retorna a string do objeto em caso de falha
        }
    }
}
