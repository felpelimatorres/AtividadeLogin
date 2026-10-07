package br.edu.umc.loginseguro.user.domain;

/**
 * Perfis de acesso do sistema.
 * <p>
 * Para criar um novo perfil basta adicioná-lo aqui e liberar as rotas
 * correspondentes em {@code SecurityConfig}; o restante do sistema
 * (cadastro, listagem, troca de perfil) passa a reconhecê-lo automaticamente.
 */
public enum Role {

    ADMIN("Administrador"),
    MODERADOR("Moderador"),
    USUARIO("Usuário");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** Nome da autoridade no padrão do Spring Security (ROLE_*). */
    public String authority() {
        return "ROLE_" + name();
    }
}
