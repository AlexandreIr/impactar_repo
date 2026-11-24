package br.com.impactar.course;

import java.util.UUID;

/** Projeção JPQL: quantidade de aulas (ou aulas concluídas) por curso. */
public record CourseLessonCount(UUID courseId, Long count) {
}
