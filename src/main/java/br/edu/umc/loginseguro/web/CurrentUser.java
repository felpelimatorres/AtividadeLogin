package br.edu.umc.loginseguro.web;

import java.util.Arrays;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import br.edu.umc.loginseguro.user.domain.Role;

public final class CurrentUser {

    private CurrentUser() {
    }

    public static Role role(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .flatMap(authority -> Arrays.stream(Role.values()).filter(r -> r.authority().equals(authority)))
                .findFirst()
                .orElse(Role.USUARIO);
    }
}
