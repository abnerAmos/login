package com.example.login.security;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Filtro personalizado para autenticação e autorização.
    private final SecurityFilter securityFilter;

    // EntryPoint para lidar com exceções de autenticação.
    @Qualifier("delegatedAuthenticationEntryPoint")
    private final AuthenticationEntryPoint authEntryPoint;

    public SecurityConfig(SecurityFilter securityFilter, AuthenticationEntryPoint authEntryPoint) {
        this.securityFilter = securityFilter;
        this.authEntryPoint = authEntryPoint;
    }

    /**
     * Configura a cadeia de filtros e regras de segurança da aplicação.
     *
     * @param httpSecurity Objeto HttpSecurity usado para configurar a segurança da aplicação.
     * @return Um SecurityFilterChain configurado.
     * @throws Exception Caso ocorra um erro ao configurar o HttpSecurity.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity.csrf(AbstractHttpConfigurer::disable)                                       // Desabilita CSRF, útil para APIs REST (que geralmente não usam sessões).
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))     // Configura sessão como Stateless.
                .authorizeHttpRequests(request -> {                                                     // Define as regras de autorização para rotas específicas.
                    request.requestMatchers(HttpMethod.POST, "/auth/login", "/register").permitAll();    // Permitir login e registro sem autenticação
                    request.requestMatchers(HttpMethod.GET, "/auth/validate-code", "/auth/refresh-code").permitAll();
                    request.requestMatchers("/admin/**").hasRole("ADMIN");                            // Somente ADMIN pode acessar /admin
                    request.requestMatchers("/user/**").hasRole("USER");                              // Somente USER pode acessar /user
                    request.anyRequest().authenticated();                                               // Requer autenticação para outras rotas
                })
                .exceptionHandling(exceptionHandlingConfigurer -> exceptionHandlingConfigurer.authenticationEntryPoint(authEntryPoint)) // Responsável por delegar a excessão para o ExceptionHandler.
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)                                            // Adiciona o o filtro customizado para ser chamado antes do filtro do Spring
                .build();
    }

    /**
     * Define o bean de codificador de senha (PasswordEncoder) usando BCrypt.
     *
     * @return Um PasswordEncoder que utiliza BCrypt para hashing de senhas.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configura o AuthenticationManager para gerenciar autenticações.
     *
     * @param configuration Instância de AuthenticationConfiguration usada para criar o AuthenticationManager.
     * @return Um AuthenticationManager configurado.
     * @throws Exception Caso ocorra um erro ao configurar o AuthenticationManager.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
