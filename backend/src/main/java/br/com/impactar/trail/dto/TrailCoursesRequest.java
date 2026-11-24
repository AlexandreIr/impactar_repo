package br.com.impactar.trail.dto;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

/** Lista completa de cursos da trilha, na ordem desejada (substitui a atual). */
public record TrailCoursesRequest(
        @NotNull(message = "Informe os cursos da trilha.")
        List<@NotNull UUID> courseIds
) {
}
