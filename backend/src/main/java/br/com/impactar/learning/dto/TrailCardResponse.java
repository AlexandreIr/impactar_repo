package br.com.impactar.learning.dto;

import java.util.UUID;

import br.com.impactar.learning.ProgressInfo;

public record TrailCardResponse(
        UUID id,
        String title,
        String description,
        String thumbnailUrl,
        int courseCount,
        int workloadHours,
        boolean enrolled,
        ProgressInfo progress
) {
}
