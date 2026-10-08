package br.edu.umc.loginseguro.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.edu.umc.loginseguro.config.SecurityConfig;
import br.edu.umc.loginseguro.theme.Theme;
import br.edu.umc.loginseguro.theme.ThemeService;
import br.edu.umc.loginseguro.user.domain.Role;
import br.edu.umc.loginseguro.user.dto.UserView;
import br.edu.umc.loginseguro.user.service.UserService;

@WebMvcTest
@Import(SecurityConfig.class)
class AccessControlTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private ThemeService themeService;

    @BeforeEach
    void setUp() {
        when(themeService.resolveForCurrentUser()).thenReturn(new Theme("padrao"));
        when(themeService.available()).thenReturn(List.of("padrao", "escuro"));
        UserView view = new UserView("1", "Fulano", "user", "user@exemplo.com", Role.USUARIO,
                true, false, Instant.now(), null, null);
        when(userService.findView(anyString())).thenReturn(view);
        when(userService.listAll()).thenReturn(List.of(view));
    }

    @Test
    void paginasPublicasSaoAcessiveisSemLogin() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk());
        mvc.perform(get("/login")).andExpect(status().isOk());
        mvc.perform(get("/registro")).andExpect(status().isOk());
    }

    @Test
    void anonimoEhRedirecionadoParaLogin() throws Exception {
        mvc.perform(get("/painel"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
        mvc.perform(get("/admin/usuarios"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "user", roles = "USUARIO")
    void usuarioAcessaPainelEPerfil() throws Exception {
        mvc.perform(get("/painel")).andExpect(status().isOk());
        mvc.perform(get("/usuario/perfil")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user", roles = "USUARIO")
    void usuarioNaoAcessaModeracaoNemAdministracao() throws Exception {
        mvc.perform(get("/moderador/usuarios")).andExpect(status().isForbidden());
        mvc.perform(get("/admin/usuarios")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "mod", roles = "MODERADOR")
    void moderadorAcessaModeracaoMasNaoAdministracao() throws Exception {
        mvc.perform(get("/moderador/usuarios")).andExpect(status().isOk());
        mvc.perform(get("/admin/usuarios")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminAcessaTodasAsAreas() throws Exception {
        mvc.perform(get("/painel")).andExpect(status().isOk());
        mvc.perform(get("/moderador/usuarios")).andExpect(status().isOk());
        mvc.perform(get("/admin/usuarios")).andExpect(status().isOk());
    }

    @Test
    void cadastroSemTokenCsrfEhRejeitado() throws Exception {
        mvc.perform(post("/registro").param("username", "x"))
                .andExpect(status().isForbidden());
    }

    @Test
    void cadastroComDadosInvalidosVoltaParaOFormulario() throws Exception {
        mvc.perform(post("/registro").with(csrf())
                        .param("name", "Fu")
                        .param("username", "a")
                        .param("email", "nao-e-email")
                        .param("password", "123")
                        .param("confirmPassword", "456"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/registro"))
                .andExpect(model().attributeHasFieldErrors("form",
                        "name", "username", "email", "password", "confirmPassword"));
        verify(userService, never()).register(any());
    }

    @Test
    void cadastroValidoRedirecionaParaLogin() throws Exception {
        mvc.perform(post("/registro").with(csrf())
                        .param("name", "Maria Silva")
                        .param("username", "maria")
                        .param("email", "maria@exemplo.com")
                        .param("password", "Senha@Forte1")
                        .param("confirmPassword", "Senha@Forte1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registrado"));
        verify(userService).register(any());
    }

    @Test
    @WithMockUser(username = "user", roles = "USUARIO")
    void logoutRedirecionaParaLogin() throws Exception {
        mvc.perform(post("/logout").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout"));
    }
}
