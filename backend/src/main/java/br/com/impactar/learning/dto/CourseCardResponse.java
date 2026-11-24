package br.com.impactar.learning.dto;

import java.util.UUID;

import br.com.impactar.course.Course;
import br.com.impactar.learning.ProgressInfo;

public record CourseCardResponse(
        UUID id,
        String title,
        String description,
        String thumbnailUrl,
        Integer workloadHours,
        String instructorName,
        boolean enrolled,
        ProgressInfo progress
) {

    public static CourseCardResponse from(Course course, boolean enrolled, ProgressInfo progress) {
        return new CourseCardResponse(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getThumbnailUrl(),
                course.getWorkloadHours(),
                course.getInstructor().getName(),
                enrolled,
                progress == null ? ProgressInfo.EMPTY : progress);
    }
}
