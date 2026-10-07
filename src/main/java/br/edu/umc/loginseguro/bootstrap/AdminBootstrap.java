package br.edu.umc.loginseguro.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import br.edu.umc.loginseguro.user.domain.Role;
import br.edu.umc.loginseguro.user.service.UserService;
import br.edu.umc.loginseguro.user.validation.PasswordPolicy;

/**
 * Cria o primeiro administrador a partir de variáveis de ambiente
 * (ADMIN_USERNAME, ADMIN_EMAIL, ADMIN_PASSWORD) quando ainda não existe
 * nenhum ADMIN no banco. Nenhuma credencial fica fixa no código.
 */
@Component
public class AdminBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final UserService userService;
    private final String username;
    private final String email;
    private final String password;

    public AdminBootstrap(UserService userService,
                          @Value("${app.bootstrap-admin.username:}") String username,
                          @Value("${app.bootstrap-admin.email:}") String email,
                          @Value("${app.bootstrap-admin.password:}") String password) {
        this.userService = userService;
        this.username = username;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userService.hasAnyAdmin()) {
            return;
        }
        if (!StringUtils.hasText(username) || !StringUtils.hasText(email) || !StringUtils.hasText(password)) {
            log.warn("Nenhum administrador cadastrado. Defina ADMIN_USERNAME, ADMIN_EMAIL e ADMIN_PASSWORD "
                    + "no .env para criar o primeiro ADMIN automaticamente.");
            return;
        }
        if (!password.matches(PasswordPolicy.REGEX)) {
            log.error("ADMIN_PASSWORD não atende à política de senha: {}", PasswordPolicy.MESSAGE);
            return;
        }
        userService.create("Administrador", username, email, password, Role.ADMIN);
        log.info("Administrador inicial '{}' criado.", username.trim().toLowerCase());
    }
}
