package br.com.impactar.auth.reset;

import br.com.impactar.auth.reset.dto.ForgotPasswordRequest;
import br.com.impactar.auth.reset.dto.ResetPasswordRequest;
import br.com.impactar.auth.reset.dto.ValidateResetTokenRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Públicos (sem login):
 * POST /api/auth/forgot-password        { email }               -> 204 sempre
 * POST /api/auth/reset-password/validate { token }              -> 204 ou 400
 * POST /api/auth/reset-password         { token, newPassword }  -> 204 ou 400
 */
@RestController
@RequestMapping("/auth")
public class PasswordResetController {

    private final PasswordResetService service;

    public PasswordResetController(PasswordResetService service) {
        this.service = service;
    }

    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        service.requestReset(request.email());
    }

    @PostMapping("/reset-password/validate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void validate(@Valid @RequestBody ValidateResetTokenRequest request) {
        service.validate(request.token());
    }

    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        service.resetPassword(request.token(), request.newPassword());
    }
}
