package br.com.impactar.learning.dto;

import java.util.List;
import java.util.UUID;

import br.com.impactar.learning.ProgressInfo;

/** Página do curso para o aluno: dados, progresso, aula de retomada e sumário. */
public record StudentCourseResponse(
        UUID id,
        String title,
        String description,
        String thumbnailUrl,
        Integer workloadHours,
        String instructorName,
        boolean enrolled,
        ProgressInfo progress,
        UUID resumeLessonId,
        List<OutlineModule> modules
) {

    public record OutlineModule(UUID id, String title, List<OutlineLesson> lessons) {
    }

    public record OutlineLesson(UUID id, String title, boolean hasVideo, boolean completed) {
    }
}
