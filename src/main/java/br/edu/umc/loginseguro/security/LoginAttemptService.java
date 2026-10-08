package br.edu.umc.loginseguro.security;

import java.time.Clock;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.edu.umc.loginseguro.user.repository.UserRepository;

@Service
public class LoginAttemptService {

    private static final Logger log = LoggerFactory.getLogger(LoginAttemptService.class);

    private final UserRepository repository;
    private final SecurityProperties properties;
    private final Clock clock;

    @Autowired
    public LoginAttemptService(UserRepository repository, SecurityProperties properties) {
        this(repository, properties, Clock.systemUTC());
    }

    LoginAttemptService(UserRepository repository, SecurityProperties properties, Clock clock) {
        this.repository = repository;
        this.properties = properties;
        this.clock = clock;
    }

    public void loginFailed(String login) {
        repository.findByLogin(login).ifPresent(user -> {
            Instant now = clock.instant();
            if (user.getLockedUntil() != null && !user.isLockedAt(now)) {
                user.setLockedUntil(null);
                user.setFailedLoginAttempts(0);
            }
            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);
            if (attempts >= properties.maxFailedAttempts()) {
                user.setLockedUntil(now.plus(properties.lockDuration()));
                log.warn("Conta '{}' bloqueada após {} tentativas de login malsucedidas", user.getUsername(), attempts);
            }
            repository.save(user);
        });
    }

    public void loginSucceeded(String username) {
        repository.findByUsername(username).ifPresent(user -> {
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
            user.setLastLoginAt(clock.instant());
            repository.save(user);
        });
    }
}
