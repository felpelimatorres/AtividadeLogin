package br.edu.umc.loginseguro.user.dto;

import br.edu.umc.loginseguro.user.validation.FieldsMatch;
import br.edu.umc.loginseguro.user.validation.PasswordPolicy;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@FieldsMatch(field = "password", confirmField = "confirmPassword", message = "A confirmação não confere com a senha")
public class RegistrationForm {

    @NotBlank(message = "Informe seu nome")
    @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres")
    private String name;

    @NotBlank(message = "Informe um nome de usuário")
    @Pattern(regexp = "^[a-zA-Z0-9._-]{3,30}$",
            message = "Use de 3 a 30 caracteres: letras, números, ponto, hífen ou sublinhado")
    private String username;

    @NotBlank(message = "Informe seu e-mail")
    @Email(message = "E-mail inválido")
    @Size(max = 120, message = "E-mail muito longo")
    private String email;

    @NotBlank(message = "Informe uma senha")
    @Pattern(regexp = PasswordPolicy.REGEX, message = PasswordPolicy.MESSAGE)
    private String password;

    @NotBlank(message = "Confirme a senha")
    private String confirmPassword;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}
