package br.edu.umc.loginseguro.theme;

/**
 * Tema visual resolvido para a requisição atual.
 * <ul>
 *   <li>{@code layout}: template de layout usado pelas páginas (layout:decorate)</li>
 *   <li>{@code stylesheet}: folha de estilo do tema</li>
 * </ul>
 */
public final class Theme {

    private final String name;

    public Theme(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getLayout() {
        return "themes/" + name + "/layout";
    }

    public String getStylesheet() {
        return "/themes/" + name + "/theme.css";
    }
}
