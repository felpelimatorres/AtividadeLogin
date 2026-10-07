package br.edu.umc.loginseguro.security;

import java.time.Instant;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import br.edu.umc.loginseguro.user.repository.UserRepository;

/**
 * Ponte entre o Spring Security e a coleção de usuários no MongoDB.
 * Aceita nome de usuário ou e-mail no campo de login.
 */
@Service
public class MongoUserDetailsService implements UserDetailsService {

    private final UserRepository repository;

    public MongoUserDetailsService(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        var user = repository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        return User.withUsername(user.getUsername())
                .password(user.getPasswordHash())
                .authorities(user.getRole().authority())
                .disabled(!user.isEnabled())
                .accountLocked(user.isLockedAt(Instant.now()))
                .build();
    }
}
