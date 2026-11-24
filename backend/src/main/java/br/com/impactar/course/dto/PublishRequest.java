package br.com.impactar.course.dto;

import jakarta.validation.constraints.NotNull;

public record PublishRequest(
        @NotNull(message = "Informe se o curso deve ser publicado.")
        Boolean published
) {
}
