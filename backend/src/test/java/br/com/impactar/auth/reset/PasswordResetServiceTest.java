package br.com.impactar.auth.reset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.impactar.shared.ApiException;
import br.com.impactar.user.Role;
import br.com.impactar.user.User;
import br.com.impactar.user.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

class PasswordResetServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-24T15:00:00Z");
    private static final String EMAIL = "ana@impactar.com.br";

    private final PasswordResetTokenRepository tokenRepository = mock(PasswordResetTokenRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final PasswordResetMailer mailer = mock(PasswordResetMailer.class);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("Ana", EMAIL, passwordEncoder.encode("Antiga@123"), Role.STUDENT);
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(tokenRepository.save(any(PasswordResetToken.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private PasswordResetService serviceAt(Instant now) {
        return new PasswordResetService(tokenRepository, userRepository, passwordEncoder, mailer,
                Clock.fixed(now, ZoneOffset.UTC), Duration.ofMinutes(10), "https://impactar.com.br/");
    }

    @Test
    void unknownEmailDoesNothingAndRevealsNothing() {
        when(userRepository.findByEmail("x@impactar.com.br")).thenReturn(Optional.empty());

        serviceAt(NOW).requestReset("x@impactar.com.br");

        verify(tokenRepository, never()).save(any());
        verify(mailer, never()).send(anyString(), anyString(), anyString(), anyLong());
    }

    @Test
    void requestStoresOnlyHashAndEmailsTenMinuteLink() {
        serviceAt(NOW).requestReset("  Ana@Impactar.com.br ");

        ArgumentCaptor<PasswordResetToken> saved = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository).save(saved.capture());
        ArgumentCaptor<String> link = ArgumentCaptor.forClass(String.class);
        verify(mailer).send(eq(EMAIL), eq("Ana"), link.capture(), eq(10L));

        String rawToken = link.getValue().substring(link.getValue().indexOf("token=") + 6);
        assertTrue(link.getValue().startsWith("https://impactar.com.br/redefinir-senha?token="));
        assertEquals(43, rawToken.length());
        assertNotEquals(rawToken, saved.getValue().getTokenHash());
        assertEquals(ResetTokens.hash(rawToken), saved.getValue().getTokenHash());
        assertEquals(NOW.plus(Duration.ofMinutes(10)), saved.getValue().getExpiresAt());
    }

    @Test
    void repeatedRequestWithinCooldownIsIgnored() {
        PasswordResetToken recent = token("qualquer", NOW.minusSeconds(30));
        when(tokenRepository.findFirstByUserIdOrderByCreatedAtDesc(user.getId())).thenReturn(Optional.of(recent));

        serviceAt(NOW).requestReset(EMAIL);

        verify(tokenRepository, never()).save(any());
        verify(mailer, never()).send(anyString(), anyString(), anyString(), anyLong());
    }

    @Test
    void newRequestInvalidatesPreviousLinks() {
        PasswordResetToken old = token("antigo", NOW.minus(Duration.ofMinutes(5)));
        when(tokenRepository.findFirstByUserIdOrderByCreatedAtDesc(user.getId())).thenReturn(Optional.of(old));
        when(tokenRepository.findByUserIdAndUsedAtIsNull(user.getId())).thenReturn(List.of(old));

        serviceAt(NOW).requestReset(EMAIL);

        assertNotNull(old.getUsedAt());
        verify(tokenRepository).save(any());
    }

    @Test
    void resetChangesPasswordAndConsumesLink() {
        PasswordResetToken token = token("segredo", NOW.minus(Duration.ofMinutes(3)));
        when(tokenRepository.findByTokenHash(ResetTokens.hash("segredo"))).thenReturn(Optional.of(token));
        when(tokenRepository.findByUserIdAndUsedAtIsNull(user.getId())).thenReturn(List.of(token));

        serviceAt(NOW).resetPassword("segredo", "NovaSenha@456");

        assertTrue(passwordEncoder.matches("NovaSenha@456", user.getPasswordHash()));
        assertFalse(user.isMustChangePassword());
        assertNotNull(token.getUsedAt());
    }

    @Test
    void expiredLinkIsRejected() {
        PasswordResetToken token = token("segredo", NOW.minus(Duration.ofMinutes(11)));
        when(tokenRepository.findByTokenHash(ResetTokens.hash("segredo"))).thenReturn(Optional.of(token));

        ApiException ex = assertThrows(ApiException.class,
                () -> serviceAt(NOW).resetPassword("segredo", "NovaSenha@456"));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertTrue(passwordEncoder.matches("Antiga@123", user.getPasswordHash()));
    }

    @Test
    void usedLinkIsRejected() {
        PasswordResetToken token = token("segredo", NOW.minus(Duration.ofMinutes(2)));
        token.markUsed(NOW.minusSeconds(30));
        when(tokenRepository.findByTokenHash(ResetTokens.hash("segredo"))).thenReturn(Optional.of(token));

        assertThrows(ApiException.class, () -> serviceAt(NOW).validate("segredo"));
    }

    @Test
    void unknownLinkIsRejected() {
        assertThrows(ApiException.class, () -> serviceAt(NOW).validate("nao-existe"));
        assertThrows(ApiException.class, () -> serviceAt(NOW).validate(" "));
    }

    /** Token criado em `createdAt`, válido por 10 minutos. */
    private PasswordResetToken token(String raw, Instant createdAt) {
        return new PasswordResetToken(user.getId(), ResetTokens.hash(raw), createdAt, createdAt.plus(Duration.ofMinutes(10)));
    }
}
