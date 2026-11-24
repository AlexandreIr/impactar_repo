package br.com.impactar.auth.reset.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Token no corpo (POST), não na URL: evita que ele apareça em logs de acesso. */
public record ValidateResetTokenRequest(
        @NotBlank(message = "Link inválido ou expirado. Solicite um novo.")
        @Size(max = 100, message = "Link inválido ou expirado. Solicite um novo.")
        String token
) {
}
