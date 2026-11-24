package br.com.impactar.learning.dto;

import java.util.List;
import java.util.UUID;

import br.com.impactar.learning.ProgressInfo;

public record StudentTrailResponse(
        UUID id,
        String title,
        String description,
        String thumbnailUrl,
        int workloadHours,
        boolean enrolled,
        ProgressInfo progress,
        List<CourseCardResponse> courses
) {
}
