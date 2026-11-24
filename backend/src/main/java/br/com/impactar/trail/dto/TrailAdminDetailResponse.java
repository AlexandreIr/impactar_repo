package br.com.impactar.trail.dto;

import java.util.List;
import java.util.UUID;

import br.com.impactar.course.Course;
import br.com.impactar.trail.Trail;

public record TrailAdminDetailResponse(
        UUID id,
        String title,
        String description,
        String thumbnailUrl,
        boolean published,
        List<TrailCourseItem> courses
) {

    public record TrailCourseItem(
            UUID courseId,
            String title,
            boolean published,
            Integer workloadHours,
            String instructorName
    ) {

        public static TrailCourseItem from(Course course) {
            return new TrailCourseItem(
                    course.getId(),
                    course.getTitle(),
                    course.isPublished(),
                    course.getWorkloadHours(),
                    course.getInstructor().getName());
        }
    }

    public static TrailAdminDetailResponse from(Trail trail) {
        return new TrailAdminDetailResponse(
                trail.getId(),
                trail.getTitle(),
                trail.getDescription(),
                trail.getThumbnailUrl(),
                trail.isPublished(),
                trail.getCourses().stream().map(tc -> TrailCourseItem.from(tc.getCourse())).toList());
    }
}
