package br.com.impactar.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MaterialRequest(
        @NotBlank(message = "Informe o nome do material.")
        @Size(max = 200, message = "O nome deve ter no máximo 200 caracteres.")
        String title,

        @NotBlank(message = "Informe o link do material.")
        @Size(max = 1000, message = "Link muito longo.")
        @Pattern(regexp = "^\\s*https?://.+", message = "O material deve ser um link http(s).")
        String url
) {
}
