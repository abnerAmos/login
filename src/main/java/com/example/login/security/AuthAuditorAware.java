package com.example.login.security;

import com.example.login.dto.request.AuthUser;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuthAuditorAware implements AuditorAware<AuthUser> {

    @Override
    public Optional<AuthUser> getCurrentAuditor() {
        // Obtém o usuário autenticado do contexto de segurança
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof AuthUser)) {
            return Optional.empty();
        }

        // Retorna o objeto AuthUser
        return Optional.of((AuthUser) authentication.getPrincipal());
    }

    public AuthUser getAuthUser() {
        return getCurrentAuditor().orElseThrow(
                () -> new IllegalStateException("Usuário não encontrado."));
    }
}
