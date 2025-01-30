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

/**
 * Classe responsável por sanitizar dados sensíveis antes de serem registrados nos logs.
 */
@Component
@RequiredArgsConstructor
public class SanitizerLogs {

    private final LogBuilder log;

    /**
     * Sanitiza um array de parâmetros antes de registrá-los no log.
     *
     * @param parameters Array de objetos a serem sanitizados.
     * @return Um novo array contendo os valores sanitizados.
     */
    public Object[] sanitizeParameters(Object[] parameters) {
        return Arrays.stream(parameters)
                .map(this::sanitizeObjectForLogging)
                .toArray();
    }

    /**
     * Sanitiza um objeto para registro seguro em logs.
     *
     * @param obj Objeto a ser sanitizado.
     * @return Representação segura do objeto.
     */
    public Object sanitizeObjectForLogging(Object obj) {
        if (obj == null) return null;

        // Ignorar sanitização para tipos simples e classes de pacotes do Java
        if (obj.getClass().isPrimitive() || obj instanceof String || obj instanceof Number ||
                obj instanceof Boolean || obj.getClass().getPackageName().startsWith("java.")) {
            return obj;
        }

        // Sanitiza mapas aplicando a sanitização recursivamente aos valores
        if (obj instanceof Map<?, ?> map) {
            return map.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> sanitizeObjectForLogging(entry.getValue())));
        }

        // Sanitiza coleções aplicando a sanitização recursivamente a cada elemento
        if (obj instanceof Collection<?> collection) {
            return collection.stream()
                    .map(this::sanitizeObjectForLogging)
                    .collect(Collectors.toList());
        }

        return sanitizeComplexObject(obj);
    }

    /**
     * Sanitiza objetos complexos, mascarando campos anotados como @Sensitive.
     *
     * @param obj Objeto a ser sanitizado.
     * @return Um mapa representando os campos do objeto, com dados sensíveis mascarados.
     */
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

            return sanitizedFields;
        } catch (Exception e) {
            log.warn("Falha ao sanitizar objeto para log: {}", e);
            return obj.toString();
        }
    }
}
