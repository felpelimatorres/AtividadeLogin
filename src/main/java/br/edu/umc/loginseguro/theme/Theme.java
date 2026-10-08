package br.edu.umc.loginseguro.theme;

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
