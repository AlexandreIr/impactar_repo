package br.com.impactar.course.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CourseRequest(
        @NotBlank(message = "Informe o título do curso.")
        @Size(max = 200, message = "O título deve ter no máximo 200 caracteres.")
        String title,

        @Size(max = 5000, message = "A descrição deve ter no máximo 5000 caracteres.")
        String description,

        @Size(max = 1000, message = "Link da imagem muito longo.")
        @Pattern(regexp = "^\\s*$|^https?://.+", message = "A imagem de capa deve ser um link http(s).")
        String thumbnailUrl,

        @NotNull(message = "Informe a carga horária.")
        @Min(value = 1, message = "A carga horária deve ser de pelo menos 1 hora.")
        @Max(value = 1000, message = "A carga horária deve ser de no máximo 1000 horas.")
        Integer workloadHours
) {
}
