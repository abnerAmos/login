package com.example.login.security;

import com.example.login.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Serviço responsável pela autenticação de usuários na aplicação.
 *
 * <p>Esta classe implementa a interface {@link UserDetailsService} do Spring Security,
 * que é utilizada pelo {@link AuthenticationManager} para carregar os detalhes de um usuário
 * com base no seu nome de usuário (e-mail, neste caso) durante o processo de autenticação.
 *
 * <p>O método {@code loadUserByUsername} busca o usuário no banco de dados usando o repositório {@link UserRepository}.
 * Caso o usuário não seja encontrado, é lançada uma exceção {@link UsernameNotFoundException},
 * que indica falha na autenticação.
 *
 * <p>Esta classe é marcada como um {@code @Service}, tornando-a disponível para injeção de dependência
 * e permitindo que o {@link AuthenticationManager} a utilize automaticamente durante o processo de autenticação.
 *
 * @see UserDetailsService
 * @see AuthenticationManager
 */
@Service
@RequiredArgsConstructor
public class AuthenticationService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return Optional.of(userRepository.findByEmail(username))
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com o e-mail: " + username));
    }
}
