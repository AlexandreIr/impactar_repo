package br.com.impactar.user;

import java.util.Locale;

import br.com.impactar.shared.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(name = "must_change_password", nullable = false)
    private boolean mustChangePassword = true;

    @Column(nullable = false)
    private boolean active = true;

    protected User() {
        // JPA
    }

    public User(String name, String email, String passwordHash, Role role) {
        this.name = name;
        this.email = normalizeEmail(email);
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    /** Troca feita pelo próprio usuário: libera o acesso. */
    public void changePassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
        this.mustChangePassword = false;
    }

    /** Senha provisória definida pela administração: força nova troca. */
    public void resetPassword(String temporaryPasswordHash) {
        this.passwordHash = temporaryPasswordHash;
        this.mustChangePassword = true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = normalizeEmail(email);
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isMustChangePassword() {
        return mustChangePassword;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
