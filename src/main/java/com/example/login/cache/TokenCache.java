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
     * Armazena o token no Redis com um tempo de expiração definido.
     *
     * @param userId     ID do usuário ao qual o token pertence.
     * @param token      Token JWT a ser armazenado.
     * @param expiration Tempo de expiração do token em milissegundos.
     * @param typeToken  Tipo do token, podendo ser {@code ACCESS_TOKEN} ou {@code REFRESH_TOKEN}.
     */
    public void storeToken(Long userId, String token, long expiration, String typeToken) {
        redisTemplate.opsForValue()
                .set("user:" + userId + ":" + typeToken, token, expiration, TimeUnit.MILLISECONDS);
    }

    /**
     * Recupera o token armazenado no Redis com base no tipo especificado.
     *
     * @param userId    ID do usuário ao qual o token pertence.
     * @param typeToken Tipo do token, podendo ser {@code ACCESS_TOKEN} ou {@code REFRESH_TOKEN}.
     * @return O token armazenado, ou {@code null} caso não exista.
     */
    public String getExistingToken(Long userId, String typeToken) {
        return redisTemplate.opsForValue()
                .get("user:" + userId + ":" + typeToken);
    }

    /**
     * Adiciona um token à blacklist no Redis.
     *
     * @param token O token a ser invalidado.
     */
    public void invalidateToken(String token) {
        redisTemplate.opsForSet().add("blacklist_tokens", token);
    }

    /**
     * Verifica se um token está na blacklist armazenada no Redis.
     *
     * @param token Token a ser verificado.
     * @return {@code true} se o token estiver na blacklist, caso contrário {@code false}.
     */
    public boolean isTokenInvalidated(String token) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember("blacklist_tokens", token));
    }
}
