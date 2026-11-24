package br.com.impactar.course.dto;

import java.time.Instant;
import java.util.UUID;

import br.com.impactar.course.Course;

public record CourseSummaryResponse(
        UUID id,
        String title,
        String thumbnailUrl,
        boolean published,
        Integer workloadHours,
        String instructorName,
        Instant updatedAt
) {

    public static CourseSummaryResponse from(Course course) {
        return new CourseSummaryResponse(
                course.getId(),
                course.getTitle(),
                course.getThumbnailUrl(),
                course.isPublished(),
                course.getWorkloadHours(),
                course.getInstructor().getName(),
                course.getUpdatedAt());
    }
}
