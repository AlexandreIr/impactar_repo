package br.com.impactar.learning;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import br.com.impactar.course.Course;
import br.com.impactar.course.CourseModule;
import br.com.impactar.course.Lesson;
import br.com.impactar.user.Role;
import br.com.impactar.user.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class ResumeResolverTest {

    private final UUID userId = UUID.randomUUID();
    private Lesson first;
    private Lesson second;
    private Lesson third;
    private List<Lesson> lessons;
    private final Map<UUID, LessonProgress> progress = new HashMap<>();

    @BeforeEach
    void setUp() {
        User instructor = new User("Instrutor", "instrutor@impactar.com.br", "hash", Role.INSTRUCTOR);
        CourseModule module = new CourseModule(new Course(instructor, "Curso", null, null, 10), "Módulo", 0);
        first = lesson(module, "Aula 1", 0);
        second = lesson(module, "Aula 2", 1);
        third = lesson(module, "Aula 3", 2);
        lessons = List.of(first, second, third);
    }

    @Test
    void emptyCourseHasNoResumeLesson() {
        assertTrue(ResumeResolver.resolve(List.of(), Map.of()).isEmpty());
    }

    @Test
    void newStudentStartsAtFirstLesson() {
        assertEquals(first, ResumeResolver.resolve(lessons, progress).orElseThrow());
    }

    @Test
    void resumesLastAccessedLessonWhenNotCompleted() {
        record(first, true, Instant.parse("2026-11-20T10:00:00Z"));
        record(third, false, Instant.parse("2026-11-21T10:00:00Z"));

        assertEquals(third, ResumeResolver.resolve(lessons, progress).orElseThrow());
    }

    @Test
    void goesToFirstIncompleteLessonWhenLastAccessedIsCompleted() {
        record(first, true, Instant.parse("2026-11-20T10:00:00Z"));
        record(second, true, Instant.parse("2026-11-21T10:00:00Z"));

        assertEquals(third, ResumeResolver.resolve(lessons, progress).orElseThrow());
    }

    @Test
    void completedCourseReturnsFirstLessonForReview() {
        record(first, true, Instant.parse("2026-11-20T10:00:00Z"));
        record(second, true, Instant.parse("2026-11-20T11:00:00Z"));
        record(third, true, Instant.parse("2026-11-20T12:00:00Z"));

        assertEquals(first, ResumeResolver.resolve(lessons, progress).orElseThrow());
    }

    private Lesson lesson(CourseModule module, String title, int orderIndex) {
        Lesson lesson = new Lesson(module, title, null, null, orderIndex);
        ReflectionTestUtils.setField(lesson, "id", UUID.randomUUID());
        return lesson;
    }

    private void record(Lesson lesson, boolean completed, Instant updatedAt) {
        LessonProgress entry = new LessonProgress(userId, lesson);
        if (completed) {
            entry.complete();
        }
        ReflectionTestUtils.setField(entry, "updatedAt", updatedAt);
        progress.put(lesson.getId(), entry);
    }
}
