package br.com.impactar.learning;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import br.com.impactar.course.CourseLessonCount;
import br.com.impactar.course.LessonRepository;

import org.springframework.stereotype.Component;

/** Progresso por curso em 2 consultas agregadas, para qualquer quantidade de cursos. */
@Component
public class ProgressCalculator {

    private final LessonRepository lessonRepository;
    private final LessonProgressRepository progressRepository;

    public ProgressCalculator(LessonRepository lessonRepository, LessonProgressRepository progressRepository) {
        this.lessonRepository = lessonRepository;
        this.progressRepository = progressRepository;
    }

    public Map<UUID, ProgressInfo> forCourses(UUID userId, Collection<UUID> courseIds) {
        if (courseIds.isEmpty()) {
            return Map.of();
        }
        Map<UUID, Long> totals = toMap(lessonRepository.countLessonsByCourse(courseIds));
        Map<UUID, Long> completed = toMap(progressRepository.countCompletedByCourse(userId, courseIds));

        Map<UUID, ProgressInfo> result = new HashMap<>();
        for (UUID courseId : courseIds) {
            result.put(courseId, ProgressInfo.of(completed.getOrDefault(courseId, 0L), totals.getOrDefault(courseId, 0L)));
        }
        return result;
    }

    private static Map<UUID, Long> toMap(Collection<CourseLessonCount> counts) {
        return counts.stream().collect(Collectors.toMap(CourseLessonCount::courseId, CourseLessonCount::count));
    }
}
