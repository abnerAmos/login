package com.example.login.service.impl;

import com.example.login.service.ValidationCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class ValidationCodeServiceImpl implements ValidationCodeService {

    private final CacheManager cacheManager;

    @Override
    @Cacheable(value = "validationCodes", key = "#email")
    public String generateValidationCode(String email) {
        String character = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        int codeLength = 6;

        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder(character);
        for (int i = 0; i < codeLength; i++) {
            code.append(character.charAt(random.nextInt(character.length())));
        }
        return code.toString();
    }

    @Override
    @CacheEvict(value = "validationCodes", key = "#email")
    public void invalidateValidationCode(String email) {
    }

    /**
     * Obtém o código de validação associado ao e-mail do cache.
     *
     * @param email O e-mail do usuário cujo código de validação será buscado.
     * @return O código de validação armazenado no cache ou {@code null} se não existir.
     */
    @Override
    @Cacheable(value = "validationCodes", key = "#email")
    public String getValidationCode(String email) {
        return null;
    }

    /**
     * Recupera o código de validação associado ao e-mail do cache.
     *
     * @param email O e-mail do usuário.
     * @return O código de validação ou {@code null} se não existir.
     */
    private String retrieveValidationCodeFromCache(String email) {
        Cache cache = cacheManager.getCache("validationCodes");
        if (cache == null) {
            return null;
        }
        Cache.ValueWrapper wrapper = cache.get(email);
        return wrapper != null ? (String) wrapper.get() : null;
    }
}
