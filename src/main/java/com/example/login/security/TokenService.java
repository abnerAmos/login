package com.example.login.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.login.dto.response.TokenData;
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

    @Value("${security.accessToken.expiration.minutes}")
    private Long timeExpirationAccessToken;

    @Value("${security.refreshToken.expiration.minutes}")
    private Long timeExpirationRefreshToken;

    private static final String ISSUER = "LoginModule";
    public static final String ACCESS_TOKEN = "accessToken";
    public static final String REFRESH_TOKEN = "refreshToken";

    /**
     * Gera um token JWT com base nas informações do usuário fornecido e no tipo de token especificado.
     * <p>
     * O token pode ser um {@code ACCESS_TOKEN} ou um {@code REFRESH_TOKEN}, dependendo do parâmetro {@code typeToken}.
     * A validade do token é definida com base no tipo, utilizando diferentes tempos de expiração.
     * O token gerado é assinado com o algoritmo HMAC256 e inclui informações como emissor, subject (username) e tempo de expiração.
     *
     * @param user      Objeto que contém as informações do usuário.
     * @param typeToken O tipo do token a ser gerado.
     * @return Um objeto {@link TokenData} contendo o token JWT assinado e o tempo restante de validade em milissegundos.
     * @throws InternalServerErrorException Caso ocorra um erro ao gerar o token JWT.
     */
    public TokenData generateToken(User user, String typeToken) {
        Instant expiration = typeToken.equals(ACCESS_TOKEN)
                ? tokenExpiration(timeExpirationAccessToken)
                : tokenExpiration(timeExpirationRefreshToken);

        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            String token = JWT.create()
                    .withIssuer(ISSUER)      // Define o emissor do token
                    .withSubject(user.getUsername())    // Define o subject (identificação do usuário)
                    .withExpiresAt(expiration)   // Define a data de expiração do token
                    .sign(algorithm);                   // Assina o token com o algoritmo HMAC256

            return new TokenData(token, expiration.toEpochMilli() - System.currentTimeMillis());
        } catch (JWTCreationException e){
            throw new InternalServerErrorException("Erro ao gerar" + typeToken + "jwt");
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
                    .withIssuer(ISSUER)  // Valida o emissor do token
                    .build()
                    .verify(token)      // Verifica a integridade e validade do token
                    .getSubject();      // Retorna o subject do token
        } catch (JWTVerificationException e){
            throw new AuthenticationException("Token inválido ou expirado") {};
        }
    }

    /**
     * Obtém o tempo restante até a expiração de um token JWT.
     * <p>
     * Este método decodifica o token JWT fornecido e calcula o tempo restante até a sua expiração.
     * Se o token não for válido ou estiver expirado, será lançada uma exceção.
     *
     * @param token O token JWT recebido, que será validado e decodificado.
     * @return O tempo restante até a expiração do token em milissegundos.
     * @throws RuntimeException Caso ocorra um erro ao obter a expiração do token.
     */
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
     * Calcula a data e hora de expiração do token com base no tempo de expiração fornecido.
     *
     * @param timeExpirationTypeToken Tempo de expiração em minutos.
     * @return Um {@link Instant} representando o momento da expiração do token.
     */
    private Instant tokenExpiration(Long timeExpirationTypeToken) {
        return LocalDateTime.now()
                .plusMinutes(timeExpirationTypeToken)
                .toInstant(ZoneOffset.of("-03:00"));
    }
}
