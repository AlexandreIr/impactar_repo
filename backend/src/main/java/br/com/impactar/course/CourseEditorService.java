package br.com.impactar.course;

import java.util.List;
import java.util.UUID;

import br.com.impactar.auth.CurrentUser;
import br.com.impactar.course.dto.CourseDetailResponse;
import br.com.impactar.course.dto.CourseRequest;
import br.com.impactar.course.dto.CourseSummaryResponse;
import br.com.impactar.course.dto.ModuleResponse;
import br.com.impactar.shared.ApiException;
import br.com.impactar.shared.Ordering;
import br.com.impactar.shared.Text;
import br.com.impactar.user.User;
import br.com.impactar.user.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Cursos e módulos (área do instrutor). Admin enxerga e edita todos os cursos. */
@Service
public class CourseEditorService {

    private final CourseRepository courseRepository;
    private final CourseModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;
    private final CourseAccessGuard guard;

    public CourseEditorService(CourseRepository courseRepository,
                               CourseModuleRepository moduleRepository,
                               LessonRepository lessonRepository,
                               UserRepository userRepository,
                               CourseAccessGuard guard) {
        this.courseRepository = courseRepository;
        this.moduleRepository = moduleRepository;
        this.lessonRepository = lessonRepository;
        this.userRepository = userRepository;
        this.guard = guard;
    }

    // ------------------------------------------------------------------ cursos

    @Transactional(readOnly = true)
    public List<CourseSummaryResponse> listCourses(CurrentUser user) {
        List<Course> courses = user.isAdmin()
                ? courseRepository.findAllByOrderByUpdatedAtDesc()
                : courseRepository.findByInstructor_IdOrderByUpdatedAtDesc(user.id());
        return courses.stream().map(CourseSummaryResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public CourseDetailResponse getCourse(UUID courseId, CurrentUser user) {
        return CourseDetailResponse.from(guard.course(courseId, user));
    }

    @Transactional
    public CourseDetailResponse createCourse(CourseRequest request, CurrentUser user) {
        User instructor = userRepository.getReferenceById(user.id());
        Course course = courseRepository.save(new Course(
                instructor,
                request.title().trim(),
                Text.trimToNull(request.description()),
                Text.trimToNull(request.thumbnailUrl()),
                request.workloadHours()));
        return CourseDetailResponse.from(course);
    }

    @Transactional
    public CourseDetailResponse updateCourse(UUID courseId, CourseRequest request, CurrentUser user) {
        Course course = guard.course(courseId, user);
        course.update(
                request.title().trim(),
                Text.trimToNull(request.description()),
                Text.trimToNull(request.thumbnailUrl()),
                request.workloadHours());
        return CourseDetailResponse.from(course);
    }

    @Transactional
    public CourseDetailResponse setPublished(UUID courseId, boolean published, CurrentUser user) {
        Course course = guard.course(courseId, user);

        if (published) {
            if (course.getWorkloadHours() == null) {
                throw ApiException.badRequest("Informe a carga horária antes de publicar.");
            }
            if (lessonRepository.countByModule_Course_IdAndVideoUrlIsNotNull(courseId) == 0) {
                throw ApiException.badRequest("Para publicar, o curso precisa de pelo menos uma aula com vídeo.");
            }
        }

        course.setPublished(published);
        return CourseDetailResponse.from(course);
    }

    @Transactional
    public void deleteCourse(UUID courseId, CurrentUser user) {
        Course course = guard.course(courseId, user);
        if (course.isPublished()) {
            throw ApiException.badRequest("Despublique o curso antes de excluí-lo.");
        }
        courseRepository.delete(course);
        courseRepository.flush();   // erros de integridade (ex.: certificado emitido) viram 409 aqui
    }

    // ------------------------------------------------------------------ módulos

    @Transactional
    public ModuleResponse addModule(UUID courseId, String title, CurrentUser user) {
        Course course = guard.course(courseId, user);
        int nextIndex = moduleRepository.findMaxOrderIndex(courseId) + 1;
        CourseModule module = moduleRepository.save(new CourseModule(course, title.trim(), nextIndex));
        return ModuleResponse.from(module);
    }

    @Transactional
    public ModuleResponse renameModule(UUID moduleId, String title, CurrentUser user) {
        CourseModule module = guard.module(moduleId, user);
        module.setTitle(title.trim());
        return ModuleResponse.from(module);
    }

    @Transactional
    public void deleteModule(UUID moduleId, CurrentUser user) {
        CourseModule module = guard.module(moduleId, user);
        moduleRepository.delete(module);
        moduleRepository.flush();
    }

    @Transactional
    public void reorderModules(UUID courseId, List<UUID> orderedIds, CurrentUser user) {
        guard.course(courseId, user);
        List<CourseModule> modules = moduleRepository.findByCourse_IdOrderByOrderIndexAsc(courseId);
        Ordering.apply(modules, orderedIds, CourseModule::getId, CourseModule::setOrderIndex);
    }
}
