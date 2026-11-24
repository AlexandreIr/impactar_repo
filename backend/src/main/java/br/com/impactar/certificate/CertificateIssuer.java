package br.com.impactar.certificate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import br.com.impactar.course.Course;
import br.com.impactar.course.CourseRepository;
import br.com.impactar.learning.EnrollmentRepository;
import br.com.impactar.learning.ProgressCalculator;
import br.com.impactar.learning.ProgressInfo;
import br.com.impactar.shared.ApiException;
import br.com.impactar.trail.Trail;
import br.com.impactar.trail.TrailCourse;
import br.com.impactar.trail.TrailEnrollmentRepository;
import br.com.impactar.trail.TrailRepository;
import br.com.impactar.user.User;
import br.com.impactar.user.UserRepository;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Emissão automática de certificados (regra do MVP):
 *   Curso:  matriculado e 100% das aulas concluídas.
 *   Trilha: inscrito na trilha e 100% em todos os cursos publicados dela (ao menos 1).
 * Idempotente: cada usuário recebe no máximo um certificado por curso e por trilha.
 */
@Component
public class CertificateIssuer {

    private static final int MAX_CODE_ATTEMPTS = 5;

    private final CertificateRepository certificateRepository;
    private final CertificateCodeGenerator codeGenerator;
    private final EnrollmentRepository enrollmentRepository;
    private final TrailEnrollmentRepository trailEnrollmentRepository;
    private final CourseRepository courseRepository;
    private final TrailRepository trailRepository;
    private final UserRepository userRepository;
    private final ProgressCalculator progressCalculator;

    public CertificateIssuer(CertificateRepository certificateRepository,
                             CertificateCodeGenerator codeGenerator,
                             EnrollmentRepository enrollmentRepository,
                             TrailEnrollmentRepository trailEnrollmentRepository,
                             CourseRepository courseRepository,
                             TrailRepository trailRepository,
                             UserRepository userRepository,
                             ProgressCalculator progressCalculator) {
        this.certificateRepository = certificateRepository;
        this.codeGenerator = codeGenerator;
        this.enrollmentRepository = enrollmentRepository;
        this.trailEnrollmentRepository = trailEnrollmentRepository;
        this.courseRepository = courseRepository;
        this.trailRepository = trailRepository;
        this.userRepository = userRepository;
        this.progressCalculator = progressCalculator;
    }

    /** Emite os certificados que o usuário já conquistou e ainda não tem. Retorna os novos. */
    @Transactional
    public List<Certificate> issuePending(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.unauthorized("Sessão inválida. Faça login novamente."));

        Set<UUID> enrolledCourseIds = enrollmentRepository.findCourseIdsByUserId(userId);
        List<Trail> trails = trailRepository.findAllById(trailEnrollmentRepository.findTrailIdsByUserId(userId));

        Set<UUID> courseIdsToCheck = new HashSet<>(enrolledCourseIds);
        trails.forEach(trail -> publishedCourses(trail).forEach(course -> courseIdsToCheck.add(course.getId())));
        Map<UUID, ProgressInfo> progress = progressCalculator.forCourses(userId, courseIdsToCheck);

        Set<UUID> certifiedCourses = certificateRepository.findCertifiedCourseIds(userId);
        Set<UUID> certifiedTrails = certificateRepository.findCertifiedTrailIds(userId);
        List<Certificate> issued = new ArrayList<>();

        for (Course course : courseRepository.findAllById(enrolledCourseIds)) {
            if (!certifiedCourses.contains(course.getId()) && isComplete(progress, course)) {
                issued.add(certificateRepository.save(Certificate.forCourse(
                        userId, course.getId(), uniqueCode(), user.getName(),
                        course.getTitle(), course.getWorkloadHours())));
            }
        }

        for (Trail trail : trails) {
            List<Course> courses = publishedCourses(trail);
            boolean complete = !courses.isEmpty() && courses.stream().allMatch(course -> isComplete(progress, course));
            if (complete && !certifiedTrails.contains(trail.getId())) {
                issued.add(certificateRepository.save(Certificate.forTrail(
                        userId, trail.getId(), uniqueCode(), user.getName(),
                        trail.getTitle(), totalHours(courses))));
            }
        }

        return issued;
    }

    private String uniqueCode() {
        for (int attempt = 0; attempt < MAX_CODE_ATTEMPTS; attempt++) {
            String code = codeGenerator.generate();
            if (!certificateRepository.existsByVerificationCode(code)) {
                return code;
            }
        }
        throw new IllegalStateException("Não foi possível gerar um código de verificação único.");
    }

    private static boolean isComplete(Map<UUID, ProgressInfo> progress, Course course) {
        return progress.getOrDefault(course.getId(), ProgressInfo.EMPTY).isComplete();
    }

    private static List<Course> publishedCourses(Trail trail) {
        return trail.getCourses().stream().map(TrailCourse::getCourse).filter(Course::isPublished).toList();
    }

    /** Soma das cargas horárias; null se nenhum curso tiver carga definida. */
    private static Integer totalHours(List<Course> courses) {
        int total = courses.stream().mapToInt(c -> c.getWorkloadHours() == null ? 0 : c.getWorkloadHours()).sum();
        return total > 0 ? total : null;
    }
}
