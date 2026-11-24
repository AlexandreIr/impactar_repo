package br.com.impactar.auth.reset;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import br.com.impactar.shared.ApiException;
import br.com.impactar.user.User;
import br.com.impactar.user.UserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * "Esqueci minha senha".
 *
 * - Resposta sempre igual, exista ou não o e-mail (não revela contas).
 * - Link de uso único, válido por app.password-reset.expiration (10 min).
 * - Novo pedido invalida links anteriores; pedidos repetidos em menos de 60 s são ignorados.
 * - Só o hash do token fica no banco.
 */
@Service
public class PasswordResetService {

    static final Duration RESEND_COOLDOWN = Duration.ofSeconds(60);
    private static final String INVALID_LINK = "Link inválido ou expirado. Solicite um novo.";

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetMailer mailer;
    private final Clock clock;
    private final Duration expiration;
    private final String resetBaseUrl;

    public PasswordResetService(PasswordResetTokenRepository tokenRepository,
                                UserRepository userRepository,
                                PasswordEncoder passwordEncoder,
                                PasswordResetMailer mailer,
                                Clock clock,
                                @Value("${app.password-reset.expiration:10m}") Duration expiration,
                                @Value("${app.frontend-url}") String frontendUrl) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailer = mailer;
        this.clock = clock;
        this.expiration = expiration;
        this.resetBaseUrl = frontendUrl.replaceAll("/+$", "") + "/redefinir-senha?token=";
    }

    @Transactional
    public void requestReset(String email) {
        Optional<User> found = userRepository.findByEmail(User.normalizeEmail(email)).filter(User::isActive);
        if (found.isEmpty()) {
            return;
        }
        User user = found.get();
        Instant now = clock.instant();

        Optional<PasswordResetToken> latest = tokenRepository.findFirstByUserIdOrderByCreatedAtDesc(user.getId());
        if (latest.isPresent() && latest.get().isUsable(now)
                && latest.get().getCreatedAt().isAfter(now.minus(RESEND_COOLDOWN))) {
            return;
        }

        tokenRepository.findByUserIdAndUsedAtIsNull(user.getId()).forEach(token -> token.markUsed(now));

        String rawToken = ResetTokens.generate();
        tokenRepository.save(new PasswordResetToken(user.getId(), ResetTokens.hash(rawToken), now, now.plus(expiration)));

        mailer.send(user.getEmail(), user.getName(), resetBaseUrl + rawToken, expiration.toMinutes());
    }

    /** Usado pela tela para avisar "link expirado" antes de o usuário digitar a nova senha. */
    @Transactional(readOnly = true)
    public void validate(String rawToken) {
        findUsableToken(rawToken, clock.instant());
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        Instant now = clock.instant();
        PasswordResetToken token = findUsableToken(rawToken, now);
        User user = userRepository.findById(token.getUserId())
                .filter(User::isActive)
                .orElseThrow(() -> ApiException.badRequest(INVALID_LINK));

        user.changePassword(passwordEncoder.encode(newPassword));
        tokenRepository.findByUserIdAndUsedAtIsNull(user.getId()).forEach(other -> other.markUsed(now));
    }

    private PasswordResetToken findUsableToken(String rawToken, Instant now) {
        if (rawToken == null || rawToken.isBlank()) {
            throw ApiException.badRequest(INVALID_LINK);
        }
        return tokenRepository.findByTokenHash(ResetTokens.hash(rawToken.trim()))
                .filter(token -> token.isUsable(now))
                .orElseThrow(() -> ApiException.badRequest(INVALID_LINK));
    }
}
