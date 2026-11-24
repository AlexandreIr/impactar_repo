package br.com.impactar.learning;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import br.com.impactar.course.Course;
import br.com.impactar.course.Lesson;

/** Regras de "continuar de onde parou" dentro de um curso. */
public final class ResumeResolver {

    private ResumeResolver() {
    }

    /** Aulas do curso na ordem de exibição (módulos e aulas já ordenados por @OrderBy). */
    public static List<Lesson> orderedLessons(Course course) {
        return course.getModules().stream()
                .flatMap(module -> module.getLessons().stream())
                .toList();
    }

    /**
     * 1. Última aula acessada, se ainda não foi concluída.
     * 2. Senão, a primeira aula não concluída na ordem do curso.
     * 3. Curso concluído: a primeira aula (revisão).
     */
    public static Optional<Lesson> resolve(List<Lesson> orderedLessons, Map<UUID, LessonProgress> progressByLesson) {
        if (orderedLessons.isEmpty()) {
            return Optional.empty();
        }

        Optional<LessonProgress> latest = latest(progressByLesson.values());
        if (latest.isPresent() && !latest.get().isCompleted()) {
            UUID lessonId = latest.get().getLesson().getId();
            Optional<Lesson> lastAccessed = orderedLessons.stream().filter(l -> l.getId().equals(lessonId)).findFirst();
            if (lastAccessed.isPresent()) {
                return lastAccessed;
            }
        }

        return orderedLessons.stream()
                .filter(lesson -> {
                    LessonProgress progress = progressByLesson.get(lesson.getId());
                    return progress == null || !progress.isCompleted();
                })
                .findFirst()
                .or(() -> Optional.of(orderedLessons.getFirst()));
    }

    private static Optional<LessonProgress> latest(Collection<LessonProgress> progress) {
        return progress.stream().max(Comparator.comparing(LessonProgress::getUpdatedAt));
    }
}
