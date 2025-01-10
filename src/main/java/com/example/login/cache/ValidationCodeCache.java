package com.example.login.cache;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
public class ValidationCodeCache {

    @CacheEvict(value = "validationCodes", key = "#email")
    public void invalidateValidationCode(String email) {
    }

    /**
     * Obtém o código de validação associado ao e-mail do cache.
     *
     * @param email O e-mail do usuário cujo código de validação será buscado.
     * @return O código de validação armazenado no cache ou {@code null} se não existir.
     */
    @Cacheable(value = "validationCodes", key = "#email")
    public String getValidationCode(String email) {
        return null;
    }
}
