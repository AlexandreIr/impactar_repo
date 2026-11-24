package br.com.impactar.user.dto;

import br.com.impactar.user.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank(message = "Informe o nome.")
        @Size(min = 3, max = 150, message = "O nome deve ter entre 3 e 150 caracteres.")
        String name,

        @NotBlank(message = "Informe o e-mail.")
        @Email(message = "E-mail inválido.")
        @Size(max = 255, message = "E-mail muito longo.")
        String email,

        @NotNull(message = "Informe o perfil.")
        Role role
) {
}
