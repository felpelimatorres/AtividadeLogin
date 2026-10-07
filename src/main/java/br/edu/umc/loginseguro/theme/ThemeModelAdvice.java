package br.edu.umc.loginseguro.theme;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Disponibiliza o tema ativo como {@code ${theme}} em todas as páginas.
 * As views usam {@code layout:decorate="~{__${theme.layout}__}"}, então trocar
 * de tema não exige mudar nenhum controller nem regra de negócio.
 */
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
