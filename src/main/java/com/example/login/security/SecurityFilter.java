package com.example.login.security;

import com.example.login.exception.ForbiddenException;
import com.example.login.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.List;

/**
 * Filtro de segurança responsável por interceptar requisições,
 * validar o token JWT e autenticar o usuário no contexto de segurança do Spring.
 */
@AllArgsConstructor
@Component
public class SecurityFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final HandlerExceptionResolver handlerExceptionResolver;

    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/auth/login",
            "/register"
    );

    /**
     * Intercepta a requisição HTTP, valida o token JWT e autentica o usuário.
     *
     * @param request  A requisição HTTP recebida.
     * @param response A resposta HTTP que será enviada.
     * @param filterChain A cadeia de filtros para continuar o processamento da requisição.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        try {
            var token = tokenRecover(request);

            if (token != null) {
                var subject = tokenService.getSubject(token);   // Valida o token e extrai o subject (e-mail do usuário)
                var user =  userRepository.findByEmail(subject);
                var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()); // Cria uma instância de autenticação para o usuário
                SecurityContextHolder.getContext().setAuthentication(authentication); // Configura o contexto de segurança do Spring com os detalhes do usuário autenticado
            }

            filterChain.doFilter(request, response); // Passa a requisição para o próximo filtro na cadeia
        } catch (ForbiddenException | AuthenticationException e) {
            handlerExceptionResolver.resolveException(request, response, null, e); // Delegar a exceção ao HandlerExceptionResolver
        }
    }

    /**
     * Recupera o token JWT do cabeçalho de autorização da requisição HTTP.
     *
     * @param request A requisição HTTP.
     * @return O token JWT extraído, ou null se o cabeçalho não estiver presente.
     */
    private String tokenRecover(HttpServletRequest request) {
        if (isPublicEndpoint(request)) {
            return null;
        }

        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || authorizationHeader.isEmpty()) {
            throw new ForbiddenException("Token não encontrado");
        } else {
            return authorizationHeader.replace("Bearer ", "").trim();
        }
    }

    /**
     * Verifica se o endpoint atual está na lista de rotas públicas.
     *
     * @param request A requisição HTTP.
     * @return true se o endpoint for público, false caso contrário.
     */
    private boolean isPublicEndpoint(HttpServletRequest request) {
        String path = request.getRequestURI();
        return PUBLIC_ENDPOINTS.stream().anyMatch(publicPath -> publicPath.equalsIgnoreCase(path));
    }
}
