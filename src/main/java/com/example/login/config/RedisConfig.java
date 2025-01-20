package com.example.login.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Configuração para integração com o Redis no projeto Spring.
 * <p>
 * Esta classe define uma configuração de bean para um RedisTemplate que será usado para realizar
 * operações com o Redis, como leitura e gravação de dados.
 * <p>
 * Anotação {@code @Configuration}:
 * Indica que esta classe é uma classe de configuração do Spring e pode declarar beans que serão gerenciados pelo contêiner.
 */
@Configuration
public class RedisConfig {

    /**
     * Define o bean {@code RedisTemplate} para trabalhar com o Redis.
     *
     * @param connectionFactory A fábrica de conexão do Redis fornecida pelo Spring Boot.
     *                          Esta fábrica gerencia a conexão com o servidor Redis.
     * @return Uma instância configurada de {@code RedisTemplate<String, String>} que pode ser usada
     *         para realizar operações no Redis com chaves e valores do tipo String.
     */
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        return template;
    }
}
