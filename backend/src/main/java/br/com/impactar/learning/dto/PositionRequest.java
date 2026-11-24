package br.com.impactar.learning.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PositionRequest(
        @NotNull(message = "Informe a posição do vídeo.")
        @Min(value = 0, message = "Posição inválida.")
        @Max(value = 86_400, message = "Posição inválida.")
        Integer positionSeconds
) {
}
