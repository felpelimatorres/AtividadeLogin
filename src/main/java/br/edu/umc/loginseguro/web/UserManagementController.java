package br.edu.umc.loginseguro.web;

import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.edu.umc.loginseguro.user.domain.Role;
import br.edu.umc.loginseguro.user.service.BusinessException;
import br.edu.umc.loginseguro.user.service.UserService;

abstract class UserManagementController {

    protected final UserService userService;

    protected UserManagementController(UserService userService) {
        this.userService = userService;
    }

    protected abstract String area();

    protected abstract boolean fullControl();

    protected String list(Authentication authentication, Model model) {
        model.addAttribute("users", userService.listAll());
        model.addAttribute("roles", Role.values());
        model.addAttribute("area", area());
        model.addAttribute("fullControl", fullControl());
        model.addAttribute("currentUsername", authentication.getName());
        return "gestao/usuarios";
    }

    protected String setEnabled(String id, boolean enabled, Authentication authentication, RedirectAttributes redirect) {
        return run(redirect, enabled ? "Conta ativada." : "Conta desativada.",
                () -> userService.setEnabled(id, enabled, authentication.getName(), CurrentUser.role(authentication)));
    }

    protected String unlock(String id, RedirectAttributes redirect) {
        return run(redirect, "Conta desbloqueada.", () -> userService.unlock(id));
    }

    protected String run(RedirectAttributes redirect, String successMessage, Runnable action) {
        try {
            action.run();
            redirect.addFlashAttribute("sucesso", successMessage);
        } catch (BusinessException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:" + area() + "/usuarios";
    }
}
