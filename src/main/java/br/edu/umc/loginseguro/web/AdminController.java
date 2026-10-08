package br.edu.umc.loginseguro.web;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.edu.umc.loginseguro.user.domain.Role;
import br.edu.umc.loginseguro.user.service.UserService;

@Controller
@RequestMapping("/admin")
public class AdminController extends UserManagementController {

    public AdminController(UserService userService) {
        super(userService);
    }

    @Override
    protected String area() {
        return "/admin";
    }

    @Override
    protected boolean fullControl() {
        return true;
    }

    @GetMapping("/usuarios")
    public String users(Authentication authentication, Model model) {
        return list(authentication, model);
    }

    @PostMapping("/usuarios/{id}/perfil")
    public String changeRole(@PathVariable String id, @RequestParam Role role,
                             Authentication authentication, RedirectAttributes redirect) {
        return run(redirect, "Vínculo alterado para " + role.getLabel() + ".",
                () -> userService.changeRole(id, role, authentication.getName()));
    }

    @PostMapping("/usuarios/{id}/status")
    public String status(@PathVariable String id, @RequestParam boolean enabled,
                         Authentication authentication, RedirectAttributes redirect) {
        return setEnabled(id, enabled, authentication, redirect);
    }

    @PostMapping("/usuarios/{id}/desbloquear")
    public String unlockUser(@PathVariable String id, RedirectAttributes redirect) {
        return unlock(id, redirect);
    }

    @PostMapping("/usuarios/{id}/excluir")
    public String deleteUser(@PathVariable String id, Authentication authentication, RedirectAttributes redirect) {
        return run(redirect, "Usuário excluído.", () -> userService.delete(id, authentication.getName()));
    }
}
