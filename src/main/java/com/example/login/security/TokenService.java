package com.example.login.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.login.exception.InternalServerErrorException;
import com.example.login.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Classe responsável por gerenciar operações relacionadas ao JWT (JSON Web Token),
 * como geração, validação e extração de informações.
 */
@Service
public class TokenService {

    /**
     * Segredo utilizado para assinar e verificar os tokens JWT.
     * O valor é obtido do arquivo de configuração da aplicação (application.properties).
     */
    @Value("${security.token.secret}")
    private String secret;

    @Value("${security.token.expiration.minutes}")
    private Long timeExpiration;

    /**
     * Gera um token JWT com base nas informações do usuário fornecido.
     *
     * @param user Objeto do tipo User que contém as informações do usuário (ex.: username).
     * @return Um token JWT assinado e com validade definida.
     * @throws RuntimeException Caso ocorra um erro ao gerar o token.
     */
    public String generateToken(User user) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withIssuer("Módulo de Login")      // Define o emissor do token
                    .withSubject(user.getUsername())    // Define o subject (identificação do usuário)
                    .withExpiresAt(tokenExpiration())   // Define a data de expiração do token
                    .sign(algorithm);                   // Assina o token com o algoritmo HMAC256
        } catch (JWTCreationException e){
            throw new InternalServerErrorException("Erro ao gerar token jwt");
        }
    }

    /**
     * Extrai o subject (identificação do usuário) de um token JWT.
     *
     * @param token O token JWT recebido, que será validado e decodificado.
     * @return O subject do token, normalmente o login/username do usuário.
     * @throws AuthenticationException Caso o token seja inválido ou esteja expirado.
     */
    public String getSubject(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer("Módulo de Login")  // Valida o emissor do token
                    .build()
                    .verify(token)      // Verifica a integridade e validade do token
                    .getSubject();      // Retorna o subject do token
        } catch (JWTVerificationException e){
            throw new AuthenticationException("Token inválido ou expirado") {};
        }
    }

    public long getExpiration(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            DecodedJWT decodedJWT = JWT.require(algorithm).build().verify(token);
            return decodedJWT.getExpiresAt().getTime() - System.currentTimeMillis();
        } catch (JWTVerificationException e) {
            throw new InternalServerErrorException("Erro ao obter expiração do token");
        }
    }

    /**
     * Define a data e hora de expiração para os tokens gerados.
     *
     * @return Um objeto Instant que representa o momento da expiração do token.
     */
    private Instant tokenExpiration() {
        return LocalDateTime.now()
                .plusMinutes(timeExpiration)
                .toInstant(ZoneOffset.of("-03:00"));
    }
}
