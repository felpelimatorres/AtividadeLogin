package br.edu.umc.loginseguro.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.edu.umc.loginseguro.user.domain.Role;
import br.edu.umc.loginseguro.user.domain.User;
import br.edu.umc.loginseguro.user.dto.ChangePasswordForm;
import br.edu.umc.loginseguro.user.dto.RegistrationForm;
import br.edu.umc.loginseguro.user.repository.UserRepository;
import br.edu.umc.loginseguro.user.service.BusinessException;
import br.edu.umc.loginseguro.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repository;

    // Custo baixo apenas para o teste ficar rápido
    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4);

    private UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(repository, encoder);
    }

    private RegistrationForm form() {
        RegistrationForm form = new RegistrationForm();
        form.setName("  Maria Silva ");
        form.setUsername(" Maria ");
        form.setEmail("Maria@Exemplo.com");
        form.setPassword("Senha@Forte1");
        form.setConfirmPassword("Senha@Forte1");
        return form;
    }

    @Test
    void cadastroGeraHashNormalizaDadosEDefinePerfilUsuario() {
        when(repository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = service.register(form());

        assertThat(saved.getUsername()).isEqualTo("maria");
        assertThat(saved.getEmail()).isEqualTo("maria@exemplo.com");
        assertThat(saved.getName()).isEqualTo("Maria Silva");
        assertThat(saved.getRole()).isEqualTo(Role.USUARIO);
        assertThat(saved.getPasswordHash()).isNotEqualTo("Senha@Forte1").startsWith("$2");
        assertThat(encoder.matches("Senha@Forte1", saved.getPasswordHash())).isTrue();
    }

    @Test
    void cadastroRecusaUsuarioDuplicado() {
        when(repository.existsByUsername("maria")).thenReturn(true);

        assertThatThrownBy(() -> service.register(form()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("nome de usuário");
        verify(repository, never()).save(any());
    }

    @Test
    void trocaDeSenhaExigeSenhaAtualCorreta() {
        User user = new User("Maria", "maria", "maria@exemplo.com", encoder.encode("Senha@Forte1"), Role.USUARIO);
        when(repository.findByUsername("maria")).thenReturn(Optional.of(user));

        ChangePasswordForm change = new ChangePasswordForm();
        change.setCurrentPassword("errada");
        change.setNewPassword("Outra@Senha2");
        change.setConfirmNewPassword("Outra@Senha2");

        assertThatThrownBy(() -> service.changePassword("maria", change))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Senha atual");
    }

    @Test
    void administradorNaoPodeAlterarOProprioPerfil() {
        User admin = new User("Admin", "admin", "admin@exemplo.com", "hash", Role.ADMIN);
        admin.setId("a1");
        when(repository.findById("a1")).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> service.changeRole("a1", Role.USUARIO, "admin"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void naoPermiteRemoverOUltimoAdministrador() {
        User admin = new User("Admin", "outro", "outro@exemplo.com", "hash", Role.ADMIN);
        admin.setId("a2");
        when(repository.findById("a2")).thenReturn(Optional.of(admin));
        when(repository.countByRole(Role.ADMIN)).thenReturn(1L);

        assertThatThrownBy(() -> service.delete("a2", "admin"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("pelo menos um administrador");
    }

    @Test
    void moderadorNaoPodeDesativarOutroModerador() {
        User mod = new User("Mod", "mod2", "mod2@exemplo.com", "hash", Role.MODERADOR);
        mod.setId("m2");
        when(repository.findById("m2")).thenReturn(Optional.of(mod));

        assertThatThrownBy(() -> service.setEnabled("m2", false, "mod1", Role.MODERADOR))
                .isInstanceOf(BusinessException.class);
    }
}
