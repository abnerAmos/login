package com.example.login.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component("delegatedAuthenticationEntryPoint")
public class DelegatedAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /** Resolvedor de exceções responsáveis por processar as exceções capturadas. */
    private final HandlerExceptionResolver resolver;

    public DelegatedAuthenticationEntryPoint(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.resolver = resolver;
    }

    /**
     * Método chamado automaticamente pelo Spring Security quando ocorre uma exceção de autenticação.
     * Este método delega o tratamento da exceção ao HandlerExceptionResolver.
     *
     * @param request A requisição HTTP que gerou a exceção.
     * @param response A resposta HTTP que será enviada ao cliente.
     * @param authException A exceção de autenticação ocorrida.
     * @throws IOException Se ocorrer um erro de entrada/saída.
     * @throws ServletException Se ocorrer um erro relacionado ao Servlet.
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {

        // Delegar o tratamento da exceção para o resolver configurado
        resolver.resolveException(request, response, this, authException);
    }
}
