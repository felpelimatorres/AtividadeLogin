package br.edu.umc.loginseguro.theme;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.theme")
public record ThemeProperties(
        @DefaultValue("padrao") String defaultTheme,
        @DefaultValue("padrao") List<String> available) {
}
