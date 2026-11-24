package br.com.impactar.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ModuleRequest(
        @NotBlank(message = "Informe o título do módulo.")
        @Size(max = 200, message = "O título deve ter no máximo 200 caracteres.")
        String title
) {
}
