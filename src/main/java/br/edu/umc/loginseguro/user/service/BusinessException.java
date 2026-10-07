package br.edu.umc.loginseguro.user.service;

/**
 * Violação de regra de negócio com mensagem segura para exibir ao usuário.
 * {@code field} indica o campo do formulário relacionado (pode ser null).
 */
public class BusinessException extends RuntimeException {

    private final String field;

    public BusinessException(String message) {
        this(null, message);
    }

    public BusinessException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
