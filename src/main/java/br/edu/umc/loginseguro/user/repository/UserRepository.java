package br.edu.umc.loginseguro.user.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import br.edu.umc.loginseguro.user.domain.Role;
import br.edu.umc.loginseguro.user.domain.User;

public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    long countByRole(Role role);

    default Optional<User> findByLogin(String login) {
        if (login == null) {
            return Optional.empty();
        }
        String normalized = login.trim().toLowerCase();
        return findByUsername(normalized).or(() -> findByEmail(normalized));
    }
}
