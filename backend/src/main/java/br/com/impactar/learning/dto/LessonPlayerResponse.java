package br.com.impactar.learning.dto;

import java.util.List;
import java.util.UUID;

import br.com.impactar.course.dto.MaterialResponse;

/** Aula aberta no player. lastPositionSeconds = ponto de retomada do vídeo. */
public record LessonPlayerResponse(
        UUID id,
        String title,
        String description,
        String videoId,
        List<MaterialResponse> materials,
        boolean completed,
        int lastPositionSeconds,
        UUID courseId,
        String courseTitle,
        String moduleTitle,
        UUID previousLessonId,
        UUID nextLessonId
) {
}
