package br.com.impactar.trail.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TrailRequest(
        @NotBlank(message = "Informe o título da trilha.")
        @Size(max = 200, message = "O título deve ter no máximo 200 caracteres.")
        String title,

        @Size(max = 5000, message = "A descrição deve ter no máximo 5000 caracteres.")
        String description,

        @Size(max = 1000, message = "Link da imagem muito longo.")
        @Pattern(regexp = "^\\s*$|^https?://.+", message = "A imagem de capa deve ser um link http(s).")
        String thumbnailUrl
) {
}
