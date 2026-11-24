package br.com.impactar.auth.reset.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank(message = "Link inválido ou expirado. Solicite um novo.")
        @Size(max = 100, message = "Link inválido ou expirado. Solicite um novo.")
        String token,

        @NotBlank(message = "Informe a nova senha.")
        @Size(min = 8, max = 72, message = "A nova senha deve ter entre 8 e 72 caracteres.")
        String newPassword
) {
}
