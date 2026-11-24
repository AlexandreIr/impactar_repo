package br.com.impactar.course.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import br.com.impactar.course.Course;

/** Curso completo para o editor: módulos -> aulas -> materiais, já ordenados. */
public record CourseDetailResponse(
        UUID id,
        String title,
        String description,
        String thumbnailUrl,
        boolean published,
        Integer workloadHours,
        UUID instructorId,
        String instructorName,
        Instant updatedAt,
        List<ModuleResponse> modules
) {

    public static CourseDetailResponse from(Course course) {
        return new CourseDetailResponse(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getThumbnailUrl(),
                course.isPublished(),
                course.getWorkloadHours(),
                course.getInstructor().getId(),
                course.getInstructor().getName(),
                course.getUpdatedAt(),
                course.getModules().stream().map(ModuleResponse::from).toList());
    }
}
