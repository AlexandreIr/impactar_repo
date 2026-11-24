package br.com.impactar.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Informe o e-mail.")
        @Email(message = "E-mail inválido.")
        String email,

        @NotBlank(message = "Informe a senha.")
        @Size(max = 72, message = "E-mail ou senha inválidos.")
        String password
) {
}
