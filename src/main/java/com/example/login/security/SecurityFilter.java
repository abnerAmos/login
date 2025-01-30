package com.example.login.security;

import com.example.login.cache.TokenCache;
import com.example.login.dto.request.AuthUser;
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
import java.util.stream.Stream;

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
    private final TokenCache tokenCache;

    public static final List<String> PUBLIC_ENDPOINTS_GET = List.of(
    );

    public static final List<String> PUBLIC_ENDPOINTS_POST = List.of(
            "/register",
            "/auth/login",
            "/auth/refresh-code",
            "/auth/validate-code",
            "/auth/forgot-password",
            "/auth/reset-password"
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
                // Verifica se o token está na blacklist
                if (tokenCache.isTokenInvalidated(token)) {
                    throw new ForbiddenException("Token inválido ou expirado");
                }

                var subject = tokenService.getSubject(token);   // Valida o token e extrai o subject (e-mail do usuário)
                var user =  userRepository.findByEmail(subject);
                var authUser = new AuthUser(user.getId(), user.getUsername(), user.getRoles()); // encapsula os dados de autenticação no objeto 'AuthUser'

                var authentication = new UsernamePasswordAuthenticationToken(authUser, null, user.getAuthorities()); // Cria uma instância de autenticação para o usuário
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

        List<String> allPublicEndpoints = Stream.concat(
                PUBLIC_ENDPOINTS_GET.stream(),
                PUBLIC_ENDPOINTS_POST.stream()
        ).toList();

        return allPublicEndpoints.stream().anyMatch(publicPath -> publicPath.equalsIgnoreCase(path));
    }
}
