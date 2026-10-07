package br.edu.umc.loginseguro.theme;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Propriedades {@code app.theme.*}: tema padrão e lista de temas instalados.
 * Para instalar um tema novo basta criar os arquivos e incluir o nome em {@code available}.
 */
@ConfigurationProperties(prefix = "app.theme")
public record ThemeProperties(
        @DefaultValue("padrao") String defaultTheme,
        @DefaultValue("padrao") List<String> available) {
}
