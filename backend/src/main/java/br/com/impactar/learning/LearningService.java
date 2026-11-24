package br.com.impactar.learning;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import br.com.impactar.auth.CurrentUser;
import br.com.impactar.certificate.CertificateIssuer;
import br.com.impactar.course.Course;
import br.com.impactar.course.CourseRepository;
import br.com.impactar.course.Lesson;
import br.com.impactar.course.LessonRepository;
import br.com.impactar.course.YouTube;
import br.com.impactar.course.dto.MaterialResponse;
import br.com.impactar.learning.dto.CompletionResponse;
import br.com.impactar.learning.dto.ContinueLearningResponse;
import br.com.impactar.learning.dto.CourseCardResponse;
import br.com.impactar.learning.dto.HomeResponse;
import br.com.impactar.learning.dto.LessonPlayerResponse;
import br.com.impactar.learning.dto.StudentCourseResponse;
import br.com.impactar.learning.dto.StudentCourseResponse.OutlineLesson;
import br.com.impactar.learning.dto.StudentCourseResponse.OutlineModule;
import br.com.impactar.learning.dto.StudentTrailResponse;
import br.com.impactar.learning.dto.TrailCardResponse;
import br.com.impactar.shared.ApiException;
import br.com.impactar.trail.Trail;
import br.com.impactar.trail.TrailCourse;
import br.com.impactar.trail.TrailEnrollment;
import br.com.impactar.trail.TrailEnrollmentRepository;
import br.com.impactar.trail.TrailRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Área do aluno: catálogo, matrícula, player, progresso e retomada.
 *
 * Visibilidade: curso/trilha publicado; admin e o instrutor dono também acessam rascunhos
 * (para testar o próprio curso como aluno).
 */
@Service
public class LearningService {

    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LessonProgressRepository progressRepository;
    private final TrailRepository trailRepository;
    private final TrailEnrollmentRepository trailEnrollmentRepository;
    private final ProgressCalculator progressCalculator;
    private final CertificateIssuer certificateIssuer;

    public LearningService(CourseRepository courseRepository,
                           LessonRepository lessonRepository,
                           EnrollmentRepository enrollmentRepository,
                           LessonProgressRepository progressRepository,
                           TrailRepository trailRepository,
                           TrailEnrollmentRepository trailEnrollmentRepository,
                           ProgressCalculator progressCalculator,
                           CertificateIssuer certificateIssuer) {
        this.courseRepository = courseRepository;
        this.lessonRepository = lessonRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.progressRepository = progressRepository;
        this.trailRepository = trailRepository;
        this.trailEnrollmentRepository = trailEnrollmentRepository;
        this.progressCalculator = progressCalculator;
        this.certificateIssuer = certificateIssuer;
    }

    // ================================================================== home

    @Transactional(readOnly = true)
    public HomeResponse home(CurrentUser user) {
        List<Course> courses = courseRepository.findByPublishedTrueOrderByTitleAsc();
        Set<UUID> enrolledCourseIds = enrollmentRepository.findCourseIdsByUserId(user.id());
        Map<UUID, ProgressInfo> progress = progressCalculator.forCourses(
                user.id(), courses.stream().map(Course::getId).toList());

        List<CourseCardResponse> myCourses = new ArrayList<>();
        List<CourseCardResponse> availableCourses = new ArrayList<>();
        for (Course course : courses) {
            boolean enrolled = enrolledCourseIds.contains(course.getId());
            CourseCardResponse card = CourseCardResponse.from(course, enrolled, progress.get(course.getId()));
            (enrolled ? myCourses : availableCourses).add(card);
        }

        Set<UUID> enrolledTrailIds = trailEnrollmentRepository.findTrailIdsByUserId(user.id());
        List<TrailCardResponse> myTrails = new ArrayList<>();
        List<TrailCardResponse> availableTrails = new ArrayList<>();
        for (Trail trail : trailRepository.findByPublishedTrueOrderByTitleAsc()) {
            boolean enrolled = enrolledTrailIds.contains(trail.getId());
            TrailCardResponse card = trailCard(trail, enrolled, progress);
            (enrolled ? myTrails : availableTrails).add(card);
        }

        return new HomeResponse(
                continueLearning(user.id(), enrolledCourseIds, progress).orElse(null),
                myTrails, myCourses, availableTrails, availableCourses);
    }

    // ================================================================== curso

    @Transactional(readOnly = true)
    public StudentCourseResponse course(UUID courseId, CurrentUser user) {
        Course course = visibleCourse(courseId, user);
        boolean enrolled = enrollmentRepository.existsByUserIdAndCourse_Id(user.id(), courseId);
        Map<UUID, LessonProgress> progressByLesson = progressByLesson(user.id(), courseId);

        List<Lesson> ordered = ResumeResolver.orderedLessons(course);
        long completed = ordered.stream()
                .filter(lesson -> isCompleted(progressByLesson.get(lesson.getId())))
                .count();

        List<OutlineModule> modules = course.getModules().stream()
                .map(module -> new OutlineModule(
                        module.getId(),
                        module.getTitle(),
                        module.getLessons().stream()
                                .map(lesson -> new OutlineLesson(
                                        lesson.getId(),
                                        lesson.getTitle(),
                                        lesson.getVideoUrl() != null,
                                        isCompleted(progressByLesson.get(lesson.getId()))))
                                .toList()))
                .toList();

        UUID resumeLessonId = ResumeResolver.resolve(ordered, progressByLesson).map(Lesson::getId).orElse(null);

        return new StudentCourseResponse(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getThumbnailUrl(),
                course.getWorkloadHours(),
                course.getInstructor().getName(),
                enrolled,
                ProgressInfo.of(completed, ordered.size()),
                resumeLessonId,
                modules);
    }

    @Transactional
    public StudentCourseResponse enroll(UUID courseId, CurrentUser user) {
        Course course = visibleCourse(courseId, user);
        enrollIfNeeded(user.id(), course);
        return course(courseId, user);
    }

    // ================================================================== aula

    @Transactional(readOnly = true)
    public LessonPlayerResponse lesson(UUID lessonId, CurrentUser user) {
        Lesson lesson = enrolledLesson(lessonId, user);
        Course course = lesson.getModule().getCourse();
        LessonProgress progress = progressRepository.findByUserIdAndLesson_Id(user.id(), lessonId).orElse(null);

        List<Lesson> ordered = ResumeResolver.orderedLessons(course);
        int position = indexOf(ordered, lessonId);

        return new LessonPlayerResponse(
                lesson.getId(),
                lesson.getTitle(),
                lesson.getDescription(),
                YouTube.extractVideoId(lesson.getVideoUrl()).orElse(null),
                lesson.getMaterials().stream().map(MaterialResponse::from).toList(),
                isCompleted(progress),
                progress == null ? 0 : progress.getLastPositionSeconds(),
                course.getId(),
                course.getTitle(),
                lesson.getModule().getTitle(),
                position > 0 ? ordered.get(position - 1).getId() : null,
                position >= 0 && position < ordered.size() - 1 ? ordered.get(position + 1).getId() : null);
    }

    /** Chamado pelo player a cada ~15s e ao pausar/sair; também registra a "última aula acessada". */
    @Transactional
    public void savePosition(UUID lessonId, int positionSeconds, CurrentUser user) {
        Lesson lesson = enrolledLesson(lessonId, user);
        findOrCreateProgress(user.id(), lesson).updatePosition(positionSeconds);
    }

    @Transactional
    public CompletionResponse complete(UUID lessonId, CurrentUser user) {
        Lesson lesson = enrolledLesson(lessonId, user);
        findOrCreateProgress(user.id(), lesson).complete();
        boolean certificateIssued = !certificateIssuer.issuePending(user.id()).isEmpty();
        return completionResponse(lesson, user, true, certificateIssued);
    }

    @Transactional
    public CompletionResponse uncomplete(UUID lessonId, CurrentUser user) {
        Lesson lesson = enrolledLesson(lessonId, user);
        findOrCreateProgress(user.id(), lesson).uncomplete();
        return completionResponse(lesson, user, false, false);
    }

    // ================================================================== trilha

    @Transactional(readOnly = true)
    public StudentTrailResponse trail(UUID trailId, CurrentUser user) {
        Trail trail = visibleTrail(trailId, user);
        List<Course> courses = publishedCourses(trail);
        Set<UUID> enrolledCourseIds = enrollmentRepository.findCourseIdsByUserId(user.id());
        Map<UUID, ProgressInfo> progress = progressCalculator.forCourses(
                user.id(), courses.stream().map(Course::getId).toList());

        List<CourseCardResponse> cards = courses.stream()
                .map(course -> CourseCardResponse.from(
                        course, enrolledCourseIds.contains(course.getId()), progress.get(course.getId())))
                .toList();

        return new StudentTrailResponse(
                trail.getId(),
                trail.getTitle(),
                trail.getDescription(),
                trail.getThumbnailUrl(),
                workloadHours(courses),
                trailEnrollmentRepository.existsByUserIdAndTrail_Id(user.id(), trailId),
                ProgressInfo.sum(cards.stream().map(CourseCardResponse::progress).toList()),
                cards);
    }

    /** Matricula na trilha e em todos os cursos publicados dela. */
    @Transactional
    public StudentTrailResponse enrollTrail(UUID trailId, CurrentUser user) {
        Trail trail = visibleTrail(trailId, user);
        if (!trailEnrollmentRepository.existsByUserIdAndTrail_Id(user.id(), trailId)) {
            trailEnrollmentRepository.save(new TrailEnrollment(user.id(), trail));
        }
        for (Course course : publishedCourses(trail)) {
            enrollIfNeeded(user.id(), course);
        }
        return trail(trailId, user);
    }

    // ================================================================== apoio

    private Optional<ContinueLearningResponse> continueLearning(UUID userId,
                                                                Set<UUID> enrolledCourseIds,
                                                                Map<UUID, ProgressInfo> progress) {
        for (LessonProgress recent : progressRepository.findTop10ByUserIdOrderByUpdatedAtDesc(userId)) {
            Course course = recent.getLesson().getModule().getCourse();
            ProgressInfo courseProgress = progress.get(course.getId());
            if (!course.isPublished() || !enrolledCourseIds.contains(course.getId())
                    || courseProgress == null || courseProgress.isComplete()) {
                continue;
            }
            Optional<Lesson> resume = ResumeResolver.resolve(
                    ResumeResolver.orderedLessons(course), progressByLesson(userId, course.getId()));
            if (resume.isPresent()) {
                return Optional.of(new ContinueLearningResponse(
                        course.getId(), course.getTitle(), course.getThumbnailUrl(),
                        resume.get().getId(), resume.get().getTitle(), courseProgress));
            }
        }
        return Optional.empty();
    }

    private CompletionResponse completionResponse(Lesson lesson, CurrentUser user, boolean completed,
                                                  boolean certificateIssued) {
        Course course = lesson.getModule().getCourse();
        ProgressInfo courseProgress = progressCalculator.forCourses(user.id(), List.of(course.getId()))
                .getOrDefault(course.getId(), ProgressInfo.EMPTY);

        List<Lesson> ordered = ResumeResolver.orderedLessons(course);
        int position = indexOf(ordered, lesson.getId());
        UUID next = position >= 0 && position < ordered.size() - 1 ? ordered.get(position + 1).getId() : null;
        return new CompletionResponse(completed, courseProgress, next, certificateIssued);
    }

    private TrailCardResponse trailCard(Trail trail, boolean enrolled, Map<UUID, ProgressInfo> progress) {
        List<Course> courses = publishedCourses(trail);
        ProgressInfo trailProgress = ProgressInfo.sum(courses.stream()
                .map(course -> progress.getOrDefault(course.getId(), ProgressInfo.EMPTY))
                .toList());
        return new TrailCardResponse(
                trail.getId(), trail.getTitle(), trail.getDescription(), trail.getThumbnailUrl(),
                courses.size(), workloadHours(courses), enrolled, trailProgress);
    }

    private static List<Course> publishedCourses(Trail trail) {
        return trail.getCourses().stream().map(TrailCourse::getCourse).filter(Course::isPublished).toList();
    }

    private static int workloadHours(List<Course> courses) {
        return courses.stream().mapToInt(c -> c.getWorkloadHours() == null ? 0 : c.getWorkloadHours()).sum();
    }

    private void enrollIfNeeded(UUID userId, Course course) {
        if (!enrollmentRepository.existsByUserIdAndCourse_Id(userId, course.getId())) {
            enrollmentRepository.save(new Enrollment(userId, course));
        }
    }

    private Map<UUID, LessonProgress> progressByLesson(UUID userId, UUID courseId) {
        return progressRepository.findByUserIdAndCourseId(userId, courseId).stream()
                .collect(Collectors.toMap(p -> p.getLesson().getId(), Function.identity()));
    }

    private LessonProgress findOrCreateProgress(UUID userId, Lesson lesson) {
        return progressRepository.findByUserIdAndLesson_Id(userId, lesson.getId())
                .orElseGet(() -> progressRepository.save(new LessonProgress(userId, lesson)));
    }

    private Course visibleCourse(UUID courseId, CurrentUser user) {
        return courseRepository.findById(courseId)
                .filter(course -> canSee(course, user))
                .orElseThrow(() -> ApiException.notFound("Curso não encontrado."));
    }

    private Trail visibleTrail(UUID trailId, CurrentUser user) {
        return trailRepository.findById(trailId)
                .filter(trail -> trail.isPublished() || user.isAdmin())
                .orElseThrow(() -> ApiException.notFound("Trilha não encontrada."));
    }

    private Lesson enrolledLesson(UUID lessonId, CurrentUser user) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> ApiException.notFound("Aula não encontrada."));
        Course course = lesson.getModule().getCourse();
        if (!canSee(course, user)) {
            throw ApiException.notFound("Aula não encontrada.");
        }
        if (!enrollmentRepository.existsByUserIdAndCourse_Id(user.id(), course.getId())) {
            throw ApiException.forbidden("Matricule-se no curso para acessar as aulas.");
        }
        return lesson;
    }

    private static boolean canSee(Course course, CurrentUser user) {
        return course.isPublished() || user.isAdmin() || course.isOwnedBy(user.id());
    }

    private static boolean isCompleted(LessonProgress progress) {
        return progress != null && progress.isCompleted();
    }

    private static int indexOf(List<Lesson> lessons, UUID lessonId) {
        for (int i = 0; i < lessons.size(); i++) {
            if (lessons.get(i).getId().equals(lessonId)) {
                return i;
            }
        }
        return -1;
    }
}
