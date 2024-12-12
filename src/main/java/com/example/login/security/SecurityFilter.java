package com.example.login.security;

import com.example.login.exception.ForbiddenException;
import com.example.login.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro de segurança responsável por interceptar requisições,
 * validar o token JWT e autenticar o usuário no contexto de segurança do Spring.
 */
@AllArgsConstructor
@Component
public class SecurityFilter extends OncePerRequestFilter {

    private final TokenService tokenService;

    private final UserRepository userRepository;

    /**
     * Intercepta a requisição HTTP, valida o token JWT e autentica o usuário.
     *
     * @param request  A requisição HTTP recebida.
     * @param response A resposta HTTP que será enviada.
     * @param filterChain A cadeia de filtros para continuar o processamento da requisição.
     * @throws ServletException Se ocorrer um erro durante o processamento do filtro.
     * @throws IOException Se ocorrer um erro de entrada/saída.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        var token = tokenRecover(request);
        var subject = tokenService.getSubject(token);   // Valida o token e extrai o subject (e-mail do usuário)
        var user =  userRepository.findByEmail(subject);
        var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()); // Cria uma instância de autenticação para o usuário
        SecurityContextHolder.getContext().setAuthentication(authentication); // Configura o contexto de segurança do Spring com os detalhes do usuário autenticado

        filterChain.doFilter(request, response); // Passa a requisição para o próximo filtro na cadeia
    }

    /**
     * Recupera o token JWT do cabeçalho de autorização da requisição HTTP.
     *
     * @param request A requisição HTTP.
     * @return O token JWT extraído, ou null se o cabeçalho não estiver presente.
     */
    private String tokenRecover(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || authorizationHeader.isEmpty()) {
            throw new ForbiddenException("Token não encontrado");
        } else {
            return authorizationHeader.replace("Bearer ", "");
        }

    }
}
