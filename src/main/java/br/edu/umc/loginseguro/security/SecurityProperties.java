package br.edu.umc.loginseguro.security;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** Propriedades {@code app.security.*} do application.yml. */
@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(
        @DefaultValue("5") int maxFailedAttempts,
        @DefaultValue("15m") Duration lockDuration) {
}
