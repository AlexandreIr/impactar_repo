package br.com.impactar.course;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.impactar.auth.CurrentUser;
import br.com.impactar.shared.ApiException;
import br.com.impactar.user.Role;
import br.com.impactar.user.User;
import br.com.impactar.user.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

class CourseEditorServiceTest {

    private final CourseRepository courseRepository = mock(CourseRepository.class);
    private final CourseModuleRepository moduleRepository = mock(CourseModuleRepository.class);
    private final LessonRepository lessonRepository = mock(LessonRepository.class);
    private final LessonMaterialRepository materialRepository = mock(LessonMaterialRepository.class);
    private final CourseAccessGuard guard =
            new CourseAccessGuard(courseRepository, moduleRepository, lessonRepository, materialRepository);
    private final CourseEditorService service = new CourseEditorService(
            courseRepository, moduleRepository, lessonRepository, mock(UserRepository.class), guard);

    private final UUID ownerId = UUID.randomUUID();
    private final UUID courseId = UUID.randomUUID();
    private final CurrentUser owner = new CurrentUser(ownerId, Role.INSTRUCTOR);
    private Course course;

    @BeforeEach
    void setUp() {
        User instructor = new User("Instrutora", "instrutora@impactar.com.br", "hash", Role.INSTRUCTOR);
        ReflectionTestUtils.setField(instructor, "id", ownerId);
        course = new Course(instructor, "Curso", null, null, 20);
        ReflectionTestUtils.setField(course, "id", courseId);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
    }

    @Test
    void publishRequiresAtLeastOneLessonWithVideo() {
        when(lessonRepository.countByModule_Course_IdAndVideoUrlIsNotNull(courseId)).thenReturn(0L);

        ApiException ex = assertThrows(ApiException.class, () -> service.setPublished(courseId, true, owner));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertFalse(course.isPublished());
    }

    @Test
    void publishSucceedsWithVideoLesson() {
        when(lessonRepository.countByModule_Course_IdAndVideoUrlIsNotNull(courseId)).thenReturn(1L);

        service.setPublished(courseId, true, owner);

        assertTrue(course.isPublished());
    }

    @Test
    void otherInstructorCannotAccessCourse() {
        CurrentUser other = new CurrentUser(UUID.randomUUID(), Role.INSTRUCTOR);

        ApiException ex = assertThrows(ApiException.class, () -> service.getCourse(courseId, other));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void adminCanEditAnyCourse() {
        CurrentUser admin = new CurrentUser(UUID.randomUUID(), Role.ADMIN);
        when(lessonRepository.countByModule_Course_IdAndVideoUrlIsNotNull(courseId)).thenReturn(3L);

        service.setPublished(courseId, true, admin);

        assertTrue(course.isPublished());
    }

    @Test
    void cannotDeletePublishedCourse() {
        course.setPublished(true);

        assertThrows(ApiException.class, () -> service.deleteCourse(courseId, owner));
        verify(courseRepository, never()).delete(course);
    }

    @Test
    void reorderModulesAppliesNewOrder() {
        CourseModule first = module("Introdução", 0);
        CourseModule second = module("Prática", 1);
        when(moduleRepository.findByCourse_IdOrderByOrderIndexAsc(courseId)).thenReturn(List.of(first, second));

        service.reorderModules(courseId, List.of(second.getId(), first.getId()), owner);

        assertEquals(1, first.getOrderIndex());
        assertEquals(0, second.getOrderIndex());
    }

    @Test
    void reorderModulesRejectsIncompleteList() {
        CourseModule first = module("Introdução", 0);
        CourseModule second = module("Prática", 1);
        when(moduleRepository.findByCourse_IdOrderByOrderIndexAsc(courseId)).thenReturn(List.of(first, second));

        assertThrows(ApiException.class,
                () -> service.reorderModules(courseId, List.of(second.getId()), owner));
        assertEquals(0, first.getOrderIndex());
    }

    private CourseModule module(String title, int orderIndex) {
        CourseModule module = new CourseModule(course, title, orderIndex);
        ReflectionTestUtils.setField(module, "id", UUID.randomUUID());
        return module;
    }
}
