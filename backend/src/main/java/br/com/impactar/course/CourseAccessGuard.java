package br.com.impactar.course;

import java.util.UUID;

import br.com.impactar.auth.CurrentUser;
import br.com.impactar.shared.ApiException;

import org.springframework.stereotype.Component;

/**
 * Carrega curso/módulo/aula/material garantindo que o usuário pode editá-lo:
 * o instrutor dono do curso ou qualquer administrador.
 * Sem permissão = 404 (não revela a existência do recurso).
 */
@Component
public class CourseAccessGuard {

    private final CourseRepository courseRepository;
    private final CourseModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;
    private final LessonMaterialRepository materialRepository;

    public CourseAccessGuard(CourseRepository courseRepository,
                             CourseModuleRepository moduleRepository,
                             LessonRepository lessonRepository,
                             LessonMaterialRepository materialRepository) {
        this.courseRepository = courseRepository;
        this.moduleRepository = moduleRepository;
        this.lessonRepository = lessonRepository;
        this.materialRepository = materialRepository;
    }

    public Course course(UUID courseId, CurrentUser user) {
        return courseRepository.findById(courseId)
                .filter(course -> canEdit(course, user))
                .orElseThrow(() -> ApiException.notFound("Curso não encontrado."));
    }

    public CourseModule module(UUID moduleId, CurrentUser user) {
        return moduleRepository.findById(moduleId)
                .filter(module -> canEdit(module.getCourse(), user))
                .orElseThrow(() -> ApiException.notFound("Módulo não encontrado."));
    }

    public Lesson lesson(UUID lessonId, CurrentUser user) {
        return lessonRepository.findById(lessonId)
                .filter(lesson -> canEdit(lesson.getModule().getCourse(), user))
                .orElseThrow(() -> ApiException.notFound("Aula não encontrada."));
    }

    public LessonMaterial material(UUID materialId, CurrentUser user) {
        return materialRepository.findById(materialId)
                .filter(material -> canEdit(material.getLesson().getModule().getCourse(), user))
                .orElseThrow(() -> ApiException.notFound("Material não encontrado."));
    }

    private static boolean canEdit(Course course, CurrentUser user) {
        return user.isAdmin() || course.isOwnedBy(user.id());
    }
}
