package br.com.impactar.course.dto;

import java.util.List;
import java.util.UUID;

import br.com.impactar.course.CourseModule;

public record ModuleResponse(
        UUID id,
        String title,
        int orderIndex,
        List<LessonResponse> lessons
) {

    public static ModuleResponse from(CourseModule module) {
        return new ModuleResponse(
                module.getId(),
                module.getTitle(),
                module.getOrderIndex(),
                module.getLessons().stream().map(LessonResponse::from).toList());
    }
}
