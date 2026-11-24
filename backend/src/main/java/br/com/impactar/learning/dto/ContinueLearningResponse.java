package br.com.impactar.learning.dto;

import java.util.UUID;

import br.com.impactar.learning.ProgressInfo;

/** Card "Continuar de onde parou" da home do aluno. */
public record ContinueLearningResponse(
        UUID courseId,
        String courseTitle,
        String thumbnailUrl,
        UUID lessonId,
        String lessonTitle,
        ProgressInfo progress
) {
}
