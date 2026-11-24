package br.com.impactar.certificate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import br.com.impactar.course.Course;
import br.com.impactar.course.CourseRepository;
import br.com.impactar.learning.EnrollmentRepository;
import br.com.impactar.learning.ProgressCalculator;
import br.com.impactar.learning.ProgressInfo;
import br.com.impactar.trail.Trail;
import br.com.impactar.trail.TrailCourse;
import br.com.impactar.trail.TrailEnrollmentRepository;
import br.com.impactar.trail.TrailRepository;
import br.com.impactar.user.Role;
import br.com.impactar.user.User;
import br.com.impactar.user.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class CertificateIssuerTest {

    private final CertificateRepository certificateRepository = mock(CertificateRepository.class);
    private final EnrollmentRepository enrollmentRepository = mock(EnrollmentRepository.class);
    private final TrailEnrollmentRepository trailEnrollmentRepository = mock(TrailEnrollmentRepository.class);
    private final CourseRepository courseRepository = mock(CourseRepository.class);
    private final TrailRepository trailRepository = mock(TrailRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final ProgressCalculator progressCalculator = mock(ProgressCalculator.class);

    private final CertificateIssuer issuer = new CertificateIssuer(
            certificateRepository, new CertificateCodeGenerator(), enrollmentRepository, trailEnrollmentRepository,
            courseRepository, trailRepository, userRepository, progressCalculator);

    private final UUID userId = UUID.randomUUID();
    private User instructor;

    @BeforeEach
    void setUp() {
        User student = new User("Ana Souza", "ana@impactar.com.br", "hash", Role.STUDENT);
        instructor = new User("Instrutor", "instrutor@impactar.com.br", "hash", Role.INSTRUCTOR);
        when(userRepository.findById(userId)).thenReturn(Optional.of(student));
        when(certificateRepository.save(any(Certificate.class))).thenAnswer(inv -> inv.getArgument(0));
        when(trailRepository.findAllById(any())).thenReturn(List.of());
    }

    @Test
    void issuesCourseCertificateWhenAllLessonsCompleted() {
        Course course = course("Didática", 20);
        enroll(course);
        progress(Map.of(course.getId(), ProgressInfo.of(5, 5)));

        List<Certificate> issued = issuer.issuePending(userId);

        assertEquals(1, issued.size());
        Certificate certificate = issued.getFirst();
        assertEquals(Certificate.Kind.COURSE, certificate.getKind());
        assertEquals("Ana Souza", certificate.getHolderName());
        assertEquals("Didática", certificate.getTitle());
        assertEquals(20, certificate.getWorkloadHours());
        assertTrue(certificate.getVerificationCode().matches("^.{4}-.{4}-.{4}$"));
    }

    @Test
    void doesNotIssueForIncompleteCourse() {
        Course course = course("Didática", 20);
        enroll(course);
        progress(Map.of(course.getId(), ProgressInfo.of(4, 5)));

        assertTrue(issuer.issuePending(userId).isEmpty());
        verify(certificateRepository, never()).save(any());
    }

    @Test
    void doesNotIssueTwiceForSameCourse() {
        Course course = course("Didática", 20);
        enroll(course);
        progress(Map.of(course.getId(), ProgressInfo.of(5, 5)));
        when(certificateRepository.findCertifiedCourseIds(userId)).thenReturn(Set.of(course.getId()));

        assertTrue(issuer.issuePending(userId).isEmpty());
    }

    @Test
    void issuesTrailCertificateWithSummedWorkload() {
        Course first = course("Curso A", 10);
        Course second = course("Curso B", 30);
        enroll(first, second);
        when(certificateRepository.findCertifiedCourseIds(userId)).thenReturn(Set.of(first.getId(), second.getId()));

        Trail trail = new Trail("Formação Inicial", null, null, UUID.randomUUID());
        ReflectionTestUtils.setField(trail, "id", UUID.randomUUID());
        trail.getCourses().add(new TrailCourse(trail, first, 0));
        trail.getCourses().add(new TrailCourse(trail, second, 1));
        when(trailEnrollmentRepository.findTrailIdsByUserId(userId)).thenReturn(Set.of(trail.getId()));
        when(trailRepository.findAllById(Set.of(trail.getId()))).thenReturn(List.of(trail));

        progress(Map.of(first.getId(), ProgressInfo.of(3, 3), second.getId(), ProgressInfo.of(2, 2)));

        List<Certificate> issued = issuer.issuePending(userId);

        assertEquals(1, issued.size());
        assertEquals(Certificate.Kind.TRAIL, issued.getFirst().getKind());
        assertEquals("Formação Inicial", issued.getFirst().getTitle());
        assertEquals(40, issued.getFirst().getWorkloadHours());
    }

    private Course course(String title, Integer hours) {
        Course course = new Course(instructor, title, null, null, hours);
        course.setPublished(true);
        ReflectionTestUtils.setField(course, "id", UUID.randomUUID());
        return course;
    }

    private void enroll(Course... courses) {
        Set<UUID> ids = java.util.Arrays.stream(courses).map(Course::getId).collect(java.util.stream.Collectors.toSet());
        when(enrollmentRepository.findCourseIdsByUserId(userId)).thenReturn(ids);
        when(courseRepository.findAllById(ids)).thenReturn(List.of(courses));
    }

    private void progress(Map<UUID, ProgressInfo> progress) {
        when(progressCalculator.forCourses(eq(userId), anyCollection())).thenReturn(progress);
    }
}
