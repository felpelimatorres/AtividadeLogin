package br.edu.umc.loginseguro.theme;

import java.util.List;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import br.edu.umc.loginseguro.user.domain.User;
import br.edu.umc.loginseguro.user.repository.UserRepository;

/**
 * Decide qual tema aplicar: o escolhido pelo usuário logado ou o padrão
 * configurado. Nomes desconhecidos sempre caem no tema padrão, o que impede
 * que um valor arbitrário seja usado para montar caminhos de template.
 */
@Service
public class ThemeService {

    private final ThemeProperties properties;
    private final UserRepository userRepository;

    public ThemeService(ThemeProperties properties, UserRepository userRepository) {
        this.properties = properties;
        this.userRepository = userRepository;
    }

    public Theme resolveForCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return defaultTheme();
        }
        String chosen = userRepository.findByUsername(auth.getName())
                .map(User::getTheme)
                .orElse(null);
        return resolve(chosen);
    }

    public Theme resolve(String name) {
        return isAvailable(name) ? new Theme(name) : defaultTheme();
    }

    public boolean isAvailable(String name) {
        return name != null && properties.available().contains(name);
    }

    public List<String> available() {
        return properties.available();
    }

    private Theme defaultTheme() {
        return new Theme(properties.defaultTheme());
    }
}
