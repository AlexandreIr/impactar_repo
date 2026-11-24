package br.com.impactar.trail.dto;

import java.time.Instant;
import java.util.UUID;

import br.com.impactar.trail.Trail;

public record TrailAdminSummaryResponse(
        UUID id,
        String title,
        String thumbnailUrl,
        boolean published,
        int courseCount,
        Instant updatedAt
) {

    public static TrailAdminSummaryResponse from(Trail trail) {
        return new TrailAdminSummaryResponse(
                trail.getId(),
                trail.getTitle(),
                trail.getThumbnailUrl(),
                trail.isPublished(),
                trail.getCourses().size(),
                trail.getUpdatedAt());
    }
}
