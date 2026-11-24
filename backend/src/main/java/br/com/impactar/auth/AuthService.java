package br.com.impactar.auth;

import java.util.Optional;
import java.util.UUID;

import br.com.impactar.shared.ApiException;
import br.com.impactar.user.User;
import br.com.impactar.user.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final String INVALID_CREDENTIALS = "E-mail ou senha inválidos.";
    private static final String INVALID_SESSION = "Sessão inválida. Faça login novamente.";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /** Hash fictício: e-mail inexistente leva o mesmo tempo que senha errada (evita enumeração). */
    private final String dummyHash;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.dummyHash = passwordEncoder.encode("impactar-timing-dummy-password");
    }

    @Transactional(readOnly = true)
    public User authenticate(String email, String rawPassword) {
        Optional<User> found = userRepository.findByEmail(User.normalizeEmail(email));
        if (found.isEmpty()) {
            passwordEncoder.matches(rawPassword, dummyHash);
            throw ApiException.unauthorized(INVALID_CREDENTIALS);
        }

        User user = found.get();
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash()) || !user.isActive()) {
            throw ApiException.unauthorized(INVALID_CREDENTIALS);
        }
        return user;
    }

    @Transactional(readOnly = true)
    public User getActiveUser(UUID userId) {
        return userRepository.findById(userId)
                .filter(User::isActive)
                .orElseThrow(() -> ApiException.unauthorized(INVALID_SESSION));
    }

    @Transactional
    public User changePassword(UUID userId, String currentPassword, String newPassword) {
        User user = getActiveUser(userId);

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw ApiException.badRequest("Senha atual incorreta.");
        }
        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw ApiException.badRequest("A nova senha deve ser diferente da atual.");
        }

        user.changePassword(passwordEncoder.encode(newPassword));
        return user;
    }

    @Transactional
    public User updateProfile(UUID userId, String name) {
        User user = getActiveUser(userId);
        user.setName(name.trim());
        return user;
    }
}
