package br.edu.umc.loginseguro.user.domain;

public enum Role {

    ADMIN("Administração"),
    MODERADOR("Secretaria"),
    USUARIO("Estudante");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public String authority() {
        return "ROLE_" + name();
    }
}
