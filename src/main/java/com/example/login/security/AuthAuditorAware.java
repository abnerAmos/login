package com.example.login.security;

import com.example.login.dto.request.AuthUser;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Componente que fornece o usuário autenticado para auditoria.
 * <p>
 * Esta classe implementa a interface {@link AuditorAware} para fornecer o usuário autenticado
 * no contexto de segurança como auditor, permitindo a captura de informações sobre o usuário
 * que realizou uma operação, por exemplo, ao registrar alterações em entidades para auditoria.
 * <p>
 * A auditoria geralmente utiliza esses dados para armazenar ou associar as alterações a um usuário
 * específico.
 */
@Component
public class AuthAuditorAware implements AuditorAware<AuthUser> {

    /**
     * Retorna o usuário autenticado que será utilizado como auditor.
     * <p>
     * Obtém o usuário autenticado a partir do contexto de segurança. Caso não exista um usuário
     * autenticado ou se o usuário não for do tipo esperado, retorna um valor vazio.
     *
     * @return Um {@link Optional} contendo o usuário autenticado ou vazio caso não haja um
     *         usuário autenticado válido.
     */
    @Override
    public Optional<AuthUser> getCurrentAuditor() {
        // Obtém o usuário autenticado do contexto de segurança
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null
                && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof AuthUser authUser) {
            return Optional.of(authUser);
        }

        return Optional.empty();
    }

    /**
     * Obtém o usuário autenticado de forma não opcional.
     * <p>
     * Este método retorna o usuário autenticado ou lança uma exceção se nenhum usuário
     * autenticado for encontrado. Usado quando é necessário garantir que o usuário esteja
     * presente no contexto.
     *
     * @return O usuário autenticado.
     * @throws IllegalStateException Caso não haja um usuário autenticado.
     */
    public AuthUser getAuthUser() {
        return getCurrentAuditor().orElseThrow(
                () -> new IllegalStateException("Usuário não encontrado."));
    }
}
