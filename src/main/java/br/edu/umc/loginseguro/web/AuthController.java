package br.edu.umc.loginseguro.web;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import br.edu.umc.loginseguro.user.dto.RegistrationForm;
import br.edu.umc.loginseguro.user.service.BusinessException;
import br.edu.umc.loginseguro.user.service.UserService;
import jakarta.validation.Valid;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login(Authentication authentication) {
        return isLoggedIn(authentication) ? "redirect:/painel" : "auth/login";
    }

    @GetMapping("/registro")
    public String registrationForm(Authentication authentication, @ModelAttribute("form") RegistrationForm form) {
        return isLoggedIn(authentication) ? "redirect:/painel" : "auth/registro";
    }

    @PostMapping("/registro")
    public String register(@Valid @ModelAttribute("form") RegistrationForm form, BindingResult result) {
        if (result.hasErrors()) {
            return "auth/registro";
        }
        try {
            userService.register(form);
        } catch (BusinessException e) {
            if (e.getField() != null) {
                result.rejectValue(e.getField(), "duplicado", e.getMessage());
            } else {
                result.reject("cadastro", e.getMessage());
            }
            return "auth/registro";
        }
        return "redirect:/login?registrado";
    }

    private static boolean isLoggedIn(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }
}
