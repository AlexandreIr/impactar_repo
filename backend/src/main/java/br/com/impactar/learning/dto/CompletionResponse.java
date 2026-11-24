package br.com.impactar.learning.dto;

import java.util.UUID;

import br.com.impactar.learning.ProgressInfo;

/** certificateIssued = esta conclusão gerou um certificado novo (curso ou trilha). */
public record CompletionResponse(
        boolean completed,
        ProgressInfo courseProgress,
        UUID nextLessonId,
        boolean certificateIssued
) {
}
