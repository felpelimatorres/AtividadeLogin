package br.edu.umc.loginseguro.web;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import br.edu.umc.loginseguro.user.service.UserService;

@Controller
public class HomeController {

    private final UserService userService;

    public HomeController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/painel")
    public String dashboard(Authentication authentication, Model model) {
        model.addAttribute("user", userService.findView(authentication.getName()));
        return "painel";
    }

    @RequestMapping("/acesso-negado")
    public String accessDenied() {
        return "acesso-negado";
    }
}
