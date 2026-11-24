package br.com.impactar.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LessonRequest(
        @NotBlank(message = "Informe o título da aula.")
        @Size(max = 200, message = "O título deve ter no máximo 200 caracteres.")
        String title,

        @Size(max = 10000, message = "A descrição deve ter no máximo 10000 caracteres.")
        String description,

        /* Opcional; quando informado, precisa ser um link válido do YouTube. */
        @Size(max = 1000, message = "Link do vídeo muito longo.")
        String videoUrl
) {
}
