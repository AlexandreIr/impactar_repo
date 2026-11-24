package br.com.impactar.user.dto;

import br.com.impactar.user.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** E-mail não é editável: é o login do usuário. */
public record UpdateUserRequest(
        @NotBlank(message = "Informe o nome.")
        @Size(min = 3, max = 150, message = "O nome deve ter entre 3 e 150 caracteres.")
        String name,

        @NotNull(message = "Informe o perfil.")
        Role role,

        @NotNull(message = "Informe o status.")
        Boolean active
) {
}
