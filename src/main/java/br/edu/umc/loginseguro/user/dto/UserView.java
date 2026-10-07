package br.edu.umc.loginseguro.user.dto;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import br.edu.umc.loginseguro.user.domain.Role;
import br.edu.umc.loginseguro.user.domain.User;

/**
 * Projeção somente-leitura enviada às telas. Não carrega o hash da senha,
 * evitando que ele chegue à camada de apresentação por engano.
 */
public final class UserView {

    private static final DateTimeFormatter DATE_TIME =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault());

    private final String id;
    private final String name;
    private final String username;
    private final String email;
    private final Role role;
    private final boolean enabled;
    private final boolean locked;
    private final Instant createdAt;
    private final Instant lastLoginAt;
    private final String theme;

    public UserView(String id, String name, String username, String email, Role role, boolean enabled,
                    boolean locked, Instant createdAt, Instant lastLoginAt, String theme) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.email = email;
        this.role = role;
        this.enabled = enabled;
        this.locked = locked;
        this.createdAt = createdAt;
        this.lastLoginAt = lastLoginAt;
        this.theme = theme;
    }

    public static UserView from(User user) {
        return new UserView(
                user.getId(),
                user.getName(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.isEnabled(),
                user.isLockedAt(Instant.now()),
                user.getCreatedAt(),
                user.getLastLoginAt(),
                user.getTheme());
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public Role getRole() { return role; }
    public boolean isEnabled() { return enabled; }
    public boolean isLocked() { return locked; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getLastLoginAt() { return lastLoginAt; }
    public String getTheme() { return theme; }

    public String getCreatedAtText() { return format(createdAt); }
    public String getLastLoginAtText() { return format(lastLoginAt); }

    private static String format(Instant instant) {
        return instant == null ? "—" : DATE_TIME.format(instant);
    }
}
