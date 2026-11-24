package br.gov.sp.impactar.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import br.com.impactar.auth.TemporaryPasswordGenerator;
import br.com.impactar.notification.CredentialsMailer;
import br.com.impactar.notification.EmailDeliveryException;
import br.com.impactar.shared.ApiException;
import br.com.impactar.user.Role;
import br.com.impactar.user.User;
import br.com.impactar.user.UserAdminService;
import br.com.impactar.user.UserRepository;
import br.com.impactar.user.dto.CreateUserRequest;
import br.com.impactar.user.dto.UpdateUserRequest;
import br.com.impactar.user.dto.UserResponse;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

class UserAdminServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);
    private final CredentialsMailer credentialsMailer = mock(CredentialsMailer.class);
    private final UserAdminService service = new UserAdminService(
            userRepository, passwordEncoder, new TemporaryPasswordGenerator(), credentialsMailer);

    @Test
    void createSavesUserWithTemporaryPasswordAndSendsEmail() {
        when(userRepository.existsByEmail("joao@sp.gov.br")).thenReturn(false);
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse response = service.create(new CreateUserRequest(" João ", " Joao@SP.gov.br ", Role.STUDENT));

        assertEquals("João", response.name());
        assertEquals("joao@sp.gov.br", response.email());
        assertTrue(response.mustChangePassword());
        verify(credentialsMailer).sendWelcome(any(User.class), anyString());
    }

    @Test
    void createRejectsDuplicateEmailWithoutSendingEmail() {
        when(userRepository.existsByEmail("joao@sp.gov.br")).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class,
                () -> service.create(new CreateUserRequest("João", "joao@sp.gov.br", Role.STUDENT)));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        verify(credentialsMailer, never()).sendWelcome(any(), anyString());
    }

    @Test
    void createFailsWhenEmailCannotBeDelivered() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        doThrow(new EmailDeliveryException("falha", null))
                .when(credentialsMailer).sendWelcome(any(User.class), anyString());

        ApiException ex = assertThrows(ApiException.class,
                () -> service.create(new CreateUserRequest("João", "joao@sp.gov.br", Role.STUDENT)));

        assertEquals(HttpStatus.BAD_GATEWAY, ex.getStatus());
    }

    @Test
    void resetPasswordForcesPasswordChangeAndSendsEmail() {
        User user = new User("Ana", "ana@sp.gov.br", passwordEncoder.encode("Antiga@123"), Role.INSTRUCTOR);
        user.changePassword(passwordEncoder.encode("Definitiva@123"));
        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        service.resetPassword(UUID.randomUUID());

        assertTrue(user.isMustChangePassword());
        assertFalse(passwordEncoder.matches("Definitiva@123", user.getPasswordHash()));
        verify(credentialsMailer).sendPasswordReset(eq(user), anyString());
    }

    @Test
    void resetPasswordRejectsInactiveUser() {
        User user = new User("Ana", "ana@sp.gov.br", "hash", Role.STUDENT);
        user.setActive(false);
        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        assertThrows(ApiException.class, () -> service.resetPassword(UUID.randomUUID()));
        verify(credentialsMailer, never()).sendPasswordReset(any(), anyString());
    }

    @Test
    void updateRejectsChangeThatWouldLockAdminOut() {
        UUID adminId = UUID.randomUUID();
        User admin = new User("Admin", "admin@sp.gov.br", "hash", Role.ADMIN);
        ReflectionTestUtils.setField(admin, "id", adminId);   // id normalmente gerado pelo JPA
        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));

        assertThrows(ApiException.class,
                () -> service.update(adminId, new UpdateUserRequest("Admin", Role.ADMIN, false), adminId));
        assertThrows(ApiException.class,
                () -> service.update(adminId, new UpdateUserRequest("Admin", Role.STUDENT, true), adminId));
        assertTrue(admin.isActive());
        assertEquals(Role.ADMIN, admin.getRole());
    }
}
