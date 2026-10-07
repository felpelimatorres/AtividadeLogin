package br.edu.umc.loginseguro.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.edu.umc.loginseguro.user.domain.Role;
import br.edu.umc.loginseguro.user.domain.User;
import br.edu.umc.loginseguro.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class LoginAttemptServiceTest {

    @Mock
    private UserRepository repository;

    private final Instant now = Instant.parse("2026-10-07T12:00:00Z");
    private final SecurityProperties properties = new SecurityProperties(3, Duration.ofMinutes(15));

    @Test
    void bloqueiaContaAposExcederTentativas() {
        User user = new User("Maria", "maria", "maria@exemplo.com", "hash", Role.USUARIO);
        when(repository.findByLogin("maria")).thenReturn(Optional.of(user));
        LoginAttemptService service =
                new LoginAttemptService(repository, properties, Clock.fixed(now, ZoneOffset.UTC));

        service.loginFailed("maria");
        service.loginFailed("maria");
        assertThat(user.isLockedAt(now)).isFalse();

        service.loginFailed("maria");
        assertThat(user.isLockedAt(now)).isTrue();
        assertThat(user.getLockedUntil()).isEqualTo(now.plus(Duration.ofMinutes(15)));
    }

    @Test
    void loginComSucessoZeraContador() {
        User user = new User("Maria", "maria", "maria@exemplo.com", "hash", Role.USUARIO);
        user.setFailedLoginAttempts(2);
        when(repository.findByUsername("maria")).thenReturn(Optional.of(user));
        LoginAttemptService service =
                new LoginAttemptService(repository, properties, Clock.fixed(now, ZoneOffset.UTC));

        service.loginSucceeded("maria");

        assertThat(user.getFailedLoginAttempts()).isZero();
        assertThat(user.getLastLoginAt()).isEqualTo(now);
    }
}
