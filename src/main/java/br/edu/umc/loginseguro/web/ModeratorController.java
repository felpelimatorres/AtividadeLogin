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

import br.edu.umc.loginseguro.user.service.UserService;

/** Área do MODERADOR: vê todos os usuários e ativa/desativa/desbloqueia contas comuns. */
@Controller
@RequestMapping("/moderador")
public class ModeratorController extends UserManagementController {

    public ModeratorController(UserService userService) {
        super(userService);
    }

    @Override
    protected String area() {
        return "/moderador";
    }

    @Override
    protected boolean fullControl() {
        return false;
    }

    @GetMapping("/usuarios")
    public String users(Authentication authentication, Model model) {
        return list(authentication, model);
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
}
