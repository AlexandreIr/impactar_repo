package br.com.impactar.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Dados que o próprio usuário pode alterar. E-mail (login) e perfil ficam com a administração. */
public record UpdateProfileRequest(
        @NotBlank(message = "Informe o nome.")
        @Size(min = 3, max = 150, message = "O nome deve ter entre 3 e 150 caracteres.")
        String name
) {
}
