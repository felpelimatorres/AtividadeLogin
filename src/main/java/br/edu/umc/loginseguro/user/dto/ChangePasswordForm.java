package br.edu.umc.loginseguro.user.dto;

import br.edu.umc.loginseguro.user.validation.FieldsMatch;
import br.edu.umc.loginseguro.user.validation.PasswordPolicy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@FieldsMatch(field = "newPassword", confirmField = "confirmNewPassword", message = "A confirmação não confere com a nova senha")
public class ChangePasswordForm {

    @NotBlank(message = "Informe a senha atual")
    private String currentPassword;

    @NotBlank(message = "Informe a nova senha")
    @Pattern(regexp = PasswordPolicy.REGEX, message = PasswordPolicy.MESSAGE)
    private String newPassword;

    @NotBlank(message = "Confirme a nova senha")
    private String confirmNewPassword;

    public String getCurrentPassword() { return currentPassword; }
    public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }

    public String getConfirmNewPassword() { return confirmNewPassword; }
    public void setConfirmNewPassword(String confirmNewPassword) { this.confirmNewPassword = confirmNewPassword; }
}
