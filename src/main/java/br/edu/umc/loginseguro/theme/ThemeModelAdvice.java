package br.edu.umc.loginseguro.theme;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class ThemeModelAdvice {

    private final ThemeService themeService;

    public ThemeModelAdvice(ThemeService themeService) {
        this.themeService = themeService;
    }

    @ModelAttribute("theme")
    public Theme theme() {
        return themeService.resolveForCurrentUser();
    }
}
