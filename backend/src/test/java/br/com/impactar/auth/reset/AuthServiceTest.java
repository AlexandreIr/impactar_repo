package br.com.impactar.auth.reset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import br.com.impactar.auth.AuthService;
import br.com.impactar.shared.ApiException;
import br.com.impactar.user.Role;
import br.com.impactar.user.User;
import br.com.impactar.user.UserRepository;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthServiceTest {

    private static final String EMAIL = "maria@educacao.sp.gov.br";
    private static final String PASSWORD = "Provisoria@123";

    private final UserRepository userRepository = mock(UserRepository.class);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);
    private final AuthService authService = new AuthService(userRepository, passwordEncoder);

    private User newUser(boolean active) {
        User user = new User("Maria", EMAIL, passwordEncoder.encode(PASSWORD), Role.STUDENT);
        user.setActive(active);
        return user;
    }

    @Test
    void authenticateNormalizesEmailAndReturnsUser() {
        User user = newUser(true);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        assertSame(user, authService.authenticate("  Maria@Educacao.SP.gov.br ", PASSWORD));
    }

    @Test
    void authenticateRejectsWrongPassword() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(newUser(true)));

        ApiException ex = assertThrows(ApiException.class, () -> authService.authenticate(EMAIL, "errada"));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
    }

    @Test
    void authenticateRejectsInactiveUser() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(newUser(false)));

        assertThrows(ApiException.class, () -> authService.authenticate(EMAIL, PASSWORD));
    }

    @Test
    void authenticateRejectsUnknownEmail() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> authService.authenticate("x@y.com", PASSWORD));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
    }

    @Test
    void changePasswordUpdatesHashAndClearsFlag() {
        User user = newUser(true);
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        assertTrue(user.isMustChangePassword());

        authService.changePassword(UUID.randomUUID(), PASSWORD, "NovaSenha@456");

        assertTrue(passwordEncoder.matches("NovaSenha@456", user.getPasswordHash()));
        assertFalse(user.isMustChangePassword());
    }

    @Test
    void changePasswordRejectsWrongCurrentPassword() {
        when(userRepository.findById(any())).thenReturn(Optional.of(newUser(true)));

        ApiException ex = assertThrows(ApiException.class,
                () -> authService.changePassword(UUID.randomUUID(), "errada", "NovaSenha@456"));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void changePasswordRejectsSamePassword() {
        when(userRepository.findById(any())).thenReturn(Optional.of(newUser(true)));

        assertThrows(ApiException.class,
                () -> authService.changePassword(UUID.randomUUID(), PASSWORD, PASSWORD));
    }
}
