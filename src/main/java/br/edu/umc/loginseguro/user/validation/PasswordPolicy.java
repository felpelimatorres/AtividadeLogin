package br.edu.umc.loginseguro.user.validation;

public final class PasswordPolicy {

    public static final int MIN = 8;
    public static final int MAX = 64;

    public static final String REGEX =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,64}$";

    public static final String MESSAGE =
            "A senha deve ter de 8 a 64 caracteres, com letra maiúscula, minúscula, número e símbolo";

    private PasswordPolicy() {
    }
}
