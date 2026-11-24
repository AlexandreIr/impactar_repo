package br.com.impactar.course.dto;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/** Todos os ids do nível (módulos do curso ou aulas do módulo) na nova ordem. */
public record ReorderRequest(
        @NotEmpty(message = "Informe a nova ordem.")
        List<@NotNull UUID> ids
) {
}
