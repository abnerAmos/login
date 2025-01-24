package com.example.login.util;

import com.example.login.exception.InternalServerErrorException;

import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * Utilitário para mascarar dados sensíveis em objetos.
 * <p>
 * Esta classe fornece métodos para identificar e mascarar campos sensíveis anotados com a
 * anotação personalizada {@code @Sensitive}. É útil em situações onde informações sensíveis
 * precisam ser omitidas ou ofuscadas, como logs ou respostas de APIs.
 */
public class SensitiveDataMasker {

    /**
     * Mascarar campos sensíveis em uma lista de objetos.
     * <p>
     * Itera sobre os objetos fornecidos e verifica se possuem campos anotados com {@code @Sensitive}.
     * Se sim, aplica a máscara; caso contrário, o objeto original é retornado.
     *
     * @param args Um array de objetos que serão processados.
     * @return Um array com os objetos mascarados, quando aplicável.
     */
    public static Object[] maskSensitiveData(Object[] args) {
        if (args == null || args.length == 0) {
            return args;
        }

        return Arrays.stream(args)
                .map(arg -> {
                    if (arg != null && hasSensitiveFields(arg)) {
                        return maskSensitiveFields(arg);
                    }
                    return arg; // Retorna o próprio argumento se não possuir @Sensitive
                })
                .toArray();
    }

    /**
     * Mascarar os campos sensíveis de um objeto.
     * <p>
     * Cria uma nova instância do objeto fornecido e substitui os valores dos campos anotados
     * com {@code @Sensitive} pelo valor "*****". Os campos não sensíveis são copiados como estão.
     *
     * @param arg O objeto original que será processado.
     * @return Uma nova instância do objeto com os campos sensíveis mascarados.
     * @throws InternalServerErrorException Caso ocorra algum erro ao acessar ou copiar os campos.
     */
    public static Object maskSensitiveFields(Object arg) {
        if (arg == null) {
            return null;
        }

        try {
            Class<?> clazz = arg.getClass();
            Object maskedInstance = clazz.getDeclaredConstructor().newInstance();

            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);

                // Copia valores do campo
                Object value = field.get(arg);

                if (field.isAnnotationPresent(Sensitive.class)) {
                    field.set(maskedInstance, "*****");
                } else {
                    field.set(maskedInstance, value);
                }
            }

            return maskedInstance;
        } catch (Exception e) {
            throw new InternalServerErrorException("Erro ao mascarar dados sensíveis: " + e.getMessage());
        }
    }

    /**
     * Verifica se um objeto possui campos anotados com {@code @Sensitive}.
     *
     * @param arg O objeto a ser verificado.
     * @return {@code true} se pelo menos um campo do objeto estiver anotado com {@code @Sensitive};
     * caso contrário, {@code false}.
     */
    private static boolean hasSensitiveFields(Object arg) {
        for (Field field : arg.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Sensitive.class)) {
                return true;
            }
        }
        return false;
    }
}

