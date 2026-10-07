package br.edu.umc.loginseguro.user.service;

import java.util.List;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.edu.umc.loginseguro.user.domain.Role;
import br.edu.umc.loginseguro.user.domain.User;
import br.edu.umc.loginseguro.user.dto.ChangePasswordForm;
import br.edu.umc.loginseguro.user.dto.RegistrationForm;
import br.edu.umc.loginseguro.user.dto.UserView;
import br.edu.umc.loginseguro.user.repository.UserRepository;

/**
 * Regras de negócio de usuários. Os métodos administrativos também são
 * protegidos por {@link PreAuthorize} (defesa em profundidade): mesmo que uma
 * rota seja liberada por engano, o serviço continua exigindo o perfil certo.
 */
@Service
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    // ------------------------------------------------------------------
    // Cadastro e conta própria
    // ------------------------------------------------------------------

    /** Cadastro público: todo novo usuário recebe o perfil USUARIO. */
    public User register(RegistrationForm form) {
        return create(form.getName(), form.getUsername(), form.getEmail(), form.getPassword(), Role.USUARIO);
    }

    /** Criação com perfil explícito (usada pelo bootstrap do administrador). */
    public User create(String name, String username, String email, String rawPassword, Role role) {
        String normalizedUsername = normalize(username);
        String normalizedEmail = normalize(email);

        if (repository.existsByUsername(normalizedUsername)) {
            throw new BusinessException("username", "Este nome de usuário já está em uso");
        }
        if (repository.existsByEmail(normalizedEmail)) {
            throw new BusinessException("email", "Este e-mail já está cadastrado");
        }

        User user = new User(name.trim(), normalizedUsername, normalizedEmail,
                passwordEncoder.encode(rawPassword), role);
        try {
            return repository.save(user);
        } catch (DuplicateKeyException e) {
            // Corrida entre duas requisições simultâneas: o índice único do MongoDB garante a regra
            throw new BusinessException("Usuário ou e-mail já cadastrado");
        }
    }

    public UserView findView(String username) {
        return UserView.from(getByUsername(username));
    }

    public void changePassword(String username, ChangePasswordForm form) {
        User user = getByUsername(username);
        if (!passwordEncoder.matches(form.getCurrentPassword(), user.getPasswordHash())) {
            throw new BusinessException("currentPassword", "Senha atual incorreta");
        }
        if (passwordEncoder.matches(form.getNewPassword(), user.getPasswordHash())) {
            throw new BusinessException("newPassword", "A nova senha deve ser diferente da atual");
        }
        user.setPasswordHash(passwordEncoder.encode(form.getNewPassword()));
        repository.save(user);
    }

    public void changeTheme(String username, String theme) {
        User user = getByUsername(username);
        user.setTheme(theme);
        repository.save(user);
    }

    // ------------------------------------------------------------------
    // Gestão de usuários (moderador / administrador)
    // ------------------------------------------------------------------

    @PreAuthorize("hasAnyRole('ADMIN','MODERADOR')")
    public List<UserView> listAll() {
        return repository.findAll(Sort.by(Sort.Direction.ASC, "username")).stream()
                .map(UserView::from)
                .toList();
    }

    /**
     * Ativa/desativa uma conta. Moderadores só podem alterar contas de perfil
     * USUARIO; ninguém pode desativar a própria conta.
     */
    @PreAuthorize("hasAnyRole('ADMIN','MODERADOR')")
    public void setEnabled(String id, boolean enabled, String actorUsername, Role actorRole) {
        User target = getById(id);
        ensureNotSelf(target, actorUsername, "Você não pode alterar o status da própria conta");
        if (actorRole != Role.ADMIN && target.getRole() != Role.USUARIO) {
            throw new BusinessException("Moderadores só podem alterar contas de usuários comuns");
        }
        if (!enabled && target.getRole() == Role.ADMIN) {
            ensureAnotherActiveAdmin(target);
        }
        target.setEnabled(enabled);
        repository.save(target);
    }

    /** Desbloqueia uma conta bloqueada por excesso de tentativas de login. */
    @PreAuthorize("hasAnyRole('ADMIN','MODERADOR')")
    public void unlock(String id) {
        User target = getById(id);
        target.setFailedLoginAttempts(0);
        target.setLockedUntil(null);
        repository.save(target);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void changeRole(String id, Role newRole, String actorUsername) {
        User target = getById(id);
        ensureNotSelf(target, actorUsername, "Você não pode alterar o próprio perfil");
        if (target.getRole() == Role.ADMIN && newRole != Role.ADMIN) {
            ensureAnotherActiveAdmin(target);
        }
        target.setRole(newRole);
        repository.save(target);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void delete(String id, String actorUsername) {
        User target = getById(id);
        ensureNotSelf(target, actorUsername, "Você não pode excluir a própria conta");
        if (target.getRole() == Role.ADMIN) {
            ensureAnotherActiveAdmin(target);
        }
        repository.delete(target);
    }

    public boolean hasAnyAdmin() {
        return repository.countByRole(Role.ADMIN) > 0;
    }

    // ------------------------------------------------------------------
    // Auxiliares
    // ------------------------------------------------------------------

    private User getByUsername(String username) {
        return repository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));
    }

    private User getById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));
    }

    private static void ensureNotSelf(User target, String actorUsername, String message) {
        if (target.getUsername().equals(actorUsername)) {
            throw new BusinessException(message);
        }
    }

    /** Impede que o sistema fique sem nenhum administrador. */
    private void ensureAnotherActiveAdmin(User target) {
        long admins = repository.countByRole(Role.ADMIN);
        if (target.getRole() == Role.ADMIN && admins <= 1) {
            throw new BusinessException("O sistema precisa manter pelo menos um administrador");
        }
    }

    private static String normalize(String value) {
        return value == null ? null : value.trim().toLowerCase();
    }
}
