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

@Service
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(RegistrationForm form) {
        return create(form.getName(), form.getUsername(), form.getEmail(), form.getPassword(), Role.USUARIO);
    }

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

    @PreAuthorize("hasAnyRole('ADMIN','MODERADOR')")
    public List<UserView> listAll() {
        return repository.findAll(Sort.by(Sort.Direction.ASC, "username")).stream()
                .map(UserView::from)
                .toList();
    }

    @PreAuthorize("hasAnyRole('ADMIN','MODERADOR')")
    public void setEnabled(String id, boolean enabled, String actorUsername, Role actorRole) {
        User target = getById(id);
        ensureNotSelf(target, actorUsername, "Você não pode alterar o status da própria conta");
        if (actorRole != Role.ADMIN && target.getRole() != Role.USUARIO) {
            throw new BusinessException("A secretaria só pode alterar contas de estudantes");
        }
        if (!enabled && target.getRole() == Role.ADMIN) {
            ensureAnotherActiveAdmin(target);
        }
        target.setEnabled(enabled);
        repository.save(target);
    }

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
