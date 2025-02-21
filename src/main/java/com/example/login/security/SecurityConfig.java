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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static com.example.login.security.SecurityFilter.PUBLIC_ENDPOINTS_GET;
import static com.example.login.security.SecurityFilter.PUBLIC_ENDPOINTS_POST;
import static org.springframework.security.config.Customizer.withDefaults;

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
        return httpSecurity
                .cors(withDefaults())                                                                   // Habilita o suporte a CORS
                .csrf(AbstractHttpConfigurer::disable)                                                  // Desabilita CSRF, útil para APIs REST (que geralmente não usam sessões).
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))     // Configura sessão como Stateless.
                .authorizeHttpRequests(request -> {                                                     // Define as regras de autorização para rotas específicas.
                    request.requestMatchers(HttpMethod.POST, PUBLIC_ENDPOINTS_POST.toArray(new String[0])).permitAll();    // Permitir login e registro sem autenticação
                    request.requestMatchers(HttpMethod.GET, PUBLIC_ENDPOINTS_GET.toArray(new String[0])).permitAll();
                    request.requestMatchers("/admin/**").hasRole("ADMIN");                            // Somente ADMIN pode acessar /admin
                    request.requestMatchers("/user/**").hasRole("USER");                              // Somente USER pode acessar /user
                    request.anyRequest().authenticated();                                               // Requer autenticação para outras rotas
                })
                .exceptionHandling(exceptionHandlingConfigurer -> exceptionHandlingConfigurer.authenticationEntryPoint(authEntryPoint)) // Responsável por delegar a excessão para o ExceptionHandler.
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)                                            // Adiciona o o filtro customizado para ser chamado antes do filtro do Spring
                .build();
    }

    /**
     * Configura a política de CORS (Cross-Origin Resource Sharing) para a aplicação.
     * <p>
     * Permite que requisições de origens específicas acessem os recursos da API,
     * definindo métodos, cabeçalhos e permissões para o envio de credenciais.
     *
     * @return Um {@link CorsConfigurationSource} contendo as configurações de CORS aplicadas a todas as rotas da API.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();                  // Cria uma nova configuração de CORS

        configuration.setAllowedOrigins(List.of("http://localhost:4200"));      // Define as origens permitidas para acessar os recursos
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));   // Especifica os métodos HTTP permitidos
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));  // Define os cabeçalhos permitidos nas requisições
        configuration.setAllowCredentials(true);                                    // Permite o envio de credenciais (cookies ou tokens)

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(); // Cria uma fonte de configuração de CORS baseada em URLs
        source.registerCorsConfiguration("/**", configuration);                  // Registra a configuração de CORS para todos os caminhos (/**)

        return source;
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
