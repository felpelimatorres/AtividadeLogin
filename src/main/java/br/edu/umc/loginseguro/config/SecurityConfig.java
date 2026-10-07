package br.edu.umc.loginseguro.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

import jakarta.servlet.DispatcherType;

/**
 * Configuração central de autenticação e autorização.
 *
 * <pre>
 *  Rota                         Perfis com acesso
 *  /, /login, /registro         público
 *  /painel, /usuario/**         USUARIO, MODERADOR, ADMIN
 *  /moderador/**                MODERADOR, ADMIN
 *  /admin/**                    ADMIN
 * </pre>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /** Nome do cookie de sessão (definido em {@link SessionConfig}). */
    public static final String SESSION_COOKIE = "LSSESSION";

    private static final String CONTENT_SECURITY_POLICY =
            "default-src 'self'; script-src 'self'; style-src 'self'; img-src 'self' data:; "
            + "font-src 'self'; form-action 'self'; frame-ancestors 'none'; base-uri 'self'; object-src 'none'";

    private static final String[] PUBLIC_PATHS = {
            "/", "/login", "/registro", "/acesso-negado", "/error",
            "/css/**", "/js/**", "/img/**", "/themes/**", "/favicon.ico"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .dispatcherTypeMatchers(DispatcherType.ERROR, DispatcherType.FORWARD).permitAll()
                .requestMatchers(PUBLIC_PATHS).permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/moderador/**").hasAnyRole("ADMIN", "MODERADOR")
                .requestMatchers("/painel", "/usuario/**").hasAnyRole("ADMIN", "MODERADOR", "USUARIO")
                .anyRequest().authenticated())

            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .defaultSuccessUrl("/painel", true)
                .failureHandler(loginFailureHandler())
                .permitAll())

            // Logout somente via POST (com token CSRF), invalidando a sessão no MongoDB
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies(SESSION_COOKIE)
                .permitAll())

            // Gera um novo ID de sessão após o login (evita session fixation)
            .sessionManagement(session -> session
                .sessionFixation(fixation -> fixation.changeSessionId()))

            .exceptionHandling(ex -> ex.accessDeniedPage("/acesso-negado"))

            // CSRF fica habilitado (padrão); o Thymeleaf injeta o token nos formulários com th:action
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp.policyDirectives(CONTENT_SECURITY_POLICY))
                .frameOptions(frame -> frame.deny())
                .referrerPolicy(ref -> ref.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.SAME_ORIGIN)));

        return http.build();
    }

    /**
     * BCrypt com fator de custo 12: hash lento e com salt aleatório por senha,
     * o que inviabiliza ataques com tabelas pré-calculadas.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /** Redireciona para a tela de login com o motivo da falha, sem revelar se o usuário existe. */
    private AuthenticationFailureHandler loginFailureHandler() {
        return (request, response, exception) -> {
            String reason;
            if (exception instanceof LockedException) {
                reason = "bloqueado";
            } else if (exception instanceof DisabledException) {
                reason = "desativado";
            } else {
                reason = "erro";
            }
            response.sendRedirect(request.getContextPath() + "/login?" + reason);
        };
    }
}
