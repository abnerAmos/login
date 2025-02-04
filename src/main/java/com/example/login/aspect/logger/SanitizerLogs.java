package com.example.login.aspect.logger;

import com.example.login.util.Sensitive;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
                .map(this::sanitizeComplexObject)
                .toArray();
    }

    /**
     * Sanitiza objetos complexos, mascarando campos anotados como @Sensitive.
     *
     * @param obj Objeto a ser sanitizado.
     * @return Um mapa representando os campos do objeto, com dados sensíveis mascarados.
     */
    public Object sanitizeComplexObject(Object obj) {
        if (obj == null) return null;

        try {
            // Cria um mapa representando os campos do objeto
            Map<String, Object> sanitizedFields = new HashMap<>();
            Field[] fields = obj.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                Object fieldValue = field.get(obj);

                // Mascara valores sensíveis
                if (field.isAnnotationPresent(Sensitive.class)) {
                    sanitizedFields.put(field.getName(), "******");
                } else {
                    sanitizedFields.put(field.getName(), fieldValue);
                }
            }

            return sanitizedFields;
        } catch (Exception e) {
            log.warn("Falha ao sanitizar objeto para log: {}", e);
            return obj.toString();
        }
    }
}
