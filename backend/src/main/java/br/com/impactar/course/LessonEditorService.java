package br.com.impactar.course;

import java.util.List;
import java.util.UUID;

import br.com.impactar.auth.CurrentUser;
import br.com.impactar.course.dto.LessonRequest;
import br.com.impactar.course.dto.LessonResponse;
import br.com.impactar.course.dto.MaterialRequest;
import br.com.impactar.course.dto.MaterialResponse;
import br.com.impactar.shared.ApiException;
import br.com.impactar.shared.Ordering;
import br.com.impactar.shared.Text;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Aulas e materiais complementares (área do instrutor). */
@Service
public class LessonEditorService {

    private final LessonRepository lessonRepository;
    private final LessonMaterialRepository materialRepository;
    private final CourseAccessGuard guard;

    public LessonEditorService(LessonRepository lessonRepository,
                               LessonMaterialRepository materialRepository,
                               CourseAccessGuard guard) {
        this.lessonRepository = lessonRepository;
        this.materialRepository = materialRepository;
        this.guard = guard;
    }

    // ------------------------------------------------------------------ aulas

    @Transactional
    public LessonResponse addLesson(UUID moduleId, LessonRequest request, CurrentUser user) {
        CourseModule module = guard.module(moduleId, user);
        int nextIndex = lessonRepository.findMaxOrderIndex(moduleId) + 1;
        Lesson lesson = lessonRepository.save(new Lesson(
                module,
                request.title().trim(),
                Text.trimToNull(request.description()),
                normalizeVideoUrl(request.videoUrl()),
                nextIndex));
        return LessonResponse.from(lesson);
    }

    @Transactional
    public LessonResponse updateLesson(UUID lessonId, LessonRequest request, CurrentUser user) {
        Lesson lesson = guard.lesson(lessonId, user);
        lesson.update(
                request.title().trim(),
                Text.trimToNull(request.description()),
                normalizeVideoUrl(request.videoUrl()));
        return LessonResponse.from(lesson);
    }

    @Transactional
    public void deleteLesson(UUID lessonId, CurrentUser user) {
        Lesson lesson = guard.lesson(lessonId, user);
        lessonRepository.delete(lesson);
        lessonRepository.flush();
    }

    @Transactional
    public void reorderLessons(UUID moduleId, List<UUID> orderedIds, CurrentUser user) {
        guard.module(moduleId, user);
        List<Lesson> lessons = lessonRepository.findByModule_IdOrderByOrderIndexAsc(moduleId);
        Ordering.apply(lessons, orderedIds, Lesson::getId, Lesson::setOrderIndex);
    }

    // ------------------------------------------------------------------ materiais

    @Transactional
    public MaterialResponse addMaterial(UUID lessonId, MaterialRequest request, CurrentUser user) {
        Lesson lesson = guard.lesson(lessonId, user);
        int nextIndex = materialRepository.findMaxOrderIndex(lessonId) + 1;
        LessonMaterial material = materialRepository.save(
                new LessonMaterial(lesson, request.title().trim(), request.url().trim(), nextIndex));
        return MaterialResponse.from(material);
    }

    @Transactional
    public void deleteMaterial(UUID materialId, CurrentUser user) {
        LessonMaterial material = guard.material(materialId, user);
        materialRepository.delete(material);
    }

    /** Vídeo opcional; se informado, guarda a URL canônica do YouTube. */
    static String normalizeVideoUrl(String videoUrl) {
        String value = Text.trimToNull(videoUrl);
        if (value == null) {
            return null;
        }
        return YouTube.extractVideoId(value)
                .map(YouTube::canonicalUrl)
                .orElseThrow(() -> ApiException.badRequest(
                        "Link do YouTube inválido. Use o link do vídeo (ex.: https://www.youtube.com/watch?v=...)."));
    }
}
