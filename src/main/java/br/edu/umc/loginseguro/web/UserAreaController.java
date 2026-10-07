package br.edu.umc.loginseguro.web;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.edu.umc.loginseguro.theme.ThemeService;
import br.edu.umc.loginseguro.user.dto.ChangePasswordForm;
import br.edu.umc.loginseguro.user.service.BusinessException;
import br.edu.umc.loginseguro.user.service.UserService;
import jakarta.validation.Valid;

/** Área de qualquer usuário autenticado: perfil, troca de senha e de tema. */
@Controller
@RequestMapping("/usuario")
public class UserAreaController {

    private final UserService userService;
    private final ThemeService themeService;

    public UserAreaController(UserService userService, ThemeService themeService) {
        this.userService = userService;
        this.themeService = themeService;
    }

    @GetMapping("/perfil")
    public String profile(Authentication authentication, Model model) {
        if (!model.containsAttribute("passwordForm")) {
            model.addAttribute("passwordForm", new ChangePasswordForm());
        }
        fillProfile(authentication, model);
        return "usuario/perfil";
    }

    @PostMapping("/senha")
    public String changePassword(Authentication authentication,
                                 @Valid @ModelAttribute("passwordForm") ChangePasswordForm form,
                                 BindingResult result, Model model, RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                userService.changePassword(authentication.getName(), form);
                redirect.addFlashAttribute("sucesso", "Senha alterada com sucesso.");
                return "redirect:/usuario/perfil";
            } catch (BusinessException e) {
                result.rejectValue(e.getField() != null ? e.getField() : "currentPassword", "senha", e.getMessage());
            }
        }
        // Nunca devolve as senhas digitadas para a tela
        form.setCurrentPassword(null);
        form.setNewPassword(null);
        form.setConfirmNewPassword(null);
        fillProfile(authentication, model);
        return "usuario/perfil";
    }

    @PostMapping("/tema")
    public String changeTheme(Authentication authentication, @RequestParam String theme,
                              RedirectAttributes redirect) {
        if (!themeService.isAvailable(theme)) {
            redirect.addFlashAttribute("erro", "Tema inválido.");
        } else {
            userService.changeTheme(authentication.getName(), theme);
            redirect.addFlashAttribute("sucesso", "Tema atualizado.");
        }
        return "redirect:/usuario/perfil";
    }

    private void fillProfile(Authentication authentication, Model model) {
        model.addAttribute("user", userService.findView(authentication.getName()));
        model.addAttribute("themes", themeService.available());
    }
}
