package com.example.login.cache;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class ValidationCodeCache {

    public static int CODE_LENGTH = 6;

    /**
     * Gera um código de validação aleatório para um e-mail e o armazena em cache.
     * <p>
     * Este método cria um código de validação composto por uma sequência aleatória de 6 caracteres alfanuméricos
     * (letras maiúsculas, minúsculas e números) e o associa ao e-mail fornecido, armazenando-o no cache.
     * O código é gerado utilizando um gerador de números seguros para evitar previsibilidade.
     *
     * @param email O e-mail do usuário para o qual o código de validação será gerado.
     *              O e-mail é utilizado como chave para armazenar o código no cache.
     * @return O código de validação gerado, composto por 6 caracteres aleatórios.
     *
     * @Cacheable:
     * - value = "validationCodes": define o cache onde os códigos de validação serão armazenados.
     * - key = "#email": utiliza o e-mail como chave para armazenar e recuperar o código.
     */
    @Cacheable(value = "validationCodes", key = "#email")
    public String generateValidationCode(String email) {
        String character = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(character.charAt(random.nextInt(character.length())));
        }
        return code.toString();
    }

    /**
     * Invalida o código de validação associado a um e-mail no cache.
     * <p>
     * Esta operação remove a entrada do cache identificada pelo e-mail fornecido.
     * Garante que o código de validação não possa mais ser utilizado após a sua expiração
     * ou após a validação ser concluída com sucesso.
     *
     * @param email O e-mail do usuário cujo código de validação será invalidado.
     */
    @CacheEvict(value = "validationCodes", key = "#email")
    public void invalidateValidationCode(String email) {
        // Também foi configurado no properties para expurgar o código a cada 10min após gerado.
    }

    /**
     * Obtém o código de validação associado a um e-mail a partir do cache.
     * <p>
     * Caso o e-mail fornecido não possua um código de validação associado no cache, este método retorna {@code null}.
     * O cache é configurado para armazenar os códigos de validação temporariamente, garantindo que o código seja
     * recuperado de forma eficiente.
     *
     * @param email O e-mail do usuário cujo código de validação será buscado.
     * @return O código de validação armazenado no cache, ou {@code null} se não existir um código associado ao e-mail.
     */
    @Cacheable(value = "validationCodes", key = "#email")
    public String getValidationCode(String email) {
        return null;
    }
}
