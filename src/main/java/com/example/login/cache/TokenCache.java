package com.example.login.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class TokenCache {

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * Armazena o token no Redis.
     *
     * @param userId O id do usuário.
     * @param token O token a ser invalidado.
     * @param expiration Tempo de expiração em segundos.
     */
    public void storeToken(Long userId, String token, long expiration) {
        redisTemplate.opsForValue().set("user:" + userId + ":token", token, expiration, TimeUnit.MILLISECONDS);
    }

    /**
     * Recupera o token no Redis, caso ele exista.
     *
     * @param userId O id do usuário.
     */
    public String getExistingToken(Long userId) {
        return redisTemplate.opsForValue().get("user:" + userId + ":token");
    }

    /**
     * Adiciona um token à blacklist no Redis.
     *
     * @param token O token a ser invalidado.
     * @param expiration Tempo de expiração em segundos.
     */
    public void invalidateToken(String token, long expiration) {
        redisTemplate.opsForValue().set(token, "invalid", expiration, TimeUnit.MILLISECONDS);
    }

    /**
     * Verifica se um token está na blacklist.
     *
     * @param token O token a ser verificado.
     * @return true se o token estiver na blacklist, caso contrário false.
     */
    public boolean isTokenInvalidated(String token) {
        return redisTemplate.hasKey(token);
    }
}
