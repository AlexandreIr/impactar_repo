package br.com.impactar.course;

import java.util.List;
import java.util.UUID;

import br.com.impactar.auth.CurrentUser;
import br.com.impactar.course.dto.CourseDetailResponse;
import br.com.impactar.course.dto.CourseRequest;
import br.com.impactar.course.dto.CourseSummaryResponse;
import br.com.impactar.course.dto.ModuleRequest;
import br.com.impactar.course.dto.ModuleResponse;
import br.com.impactar.course.dto.PublishRequest;
import br.com.impactar.course.dto.ReorderRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * ADMIN e INSTRUCTOR (regra /instructor/** no SecurityConfig).
 *
 * GET    /api/instructor/courses
 * POST   /api/instructor/courses
 * GET    /api/instructor/courses/{id}                 -> árvore completa
 * PUT    /api/instructor/courses/{id}
 * PATCH  /api/instructor/courses/{id}/publish
 * DELETE /api/instructor/courses/{id}
 * POST   /api/instructor/courses/{id}/modules
 * PUT    /api/instructor/courses/{id}/modules/order
 * PUT    /api/instructor/modules/{id}
 * DELETE /api/instructor/modules/{id}
 */
@RestController
@RequestMapping("/instructor")
public class InstructorCourseController {

    private final CourseEditorService service;

    public InstructorCourseController(CourseEditorService service) {
        this.service = service;
    }

    @GetMapping("/courses")
    public List<CourseSummaryResponse> listCourses(CurrentUser user) {
        return service.listCourses(user);
    }

    @PostMapping("/courses")
    @ResponseStatus(HttpStatus.CREATED)
    public CourseDetailResponse createCourse(@Valid @RequestBody CourseRequest request, CurrentUser user) {
        return service.createCourse(request, user);
    }

    @GetMapping("/courses/{id}")
    public CourseDetailResponse getCourse(@PathVariable("id") UUID id, CurrentUser user) {
        return service.getCourse(id, user);
    }

    @PutMapping("/courses/{id}")
    public CourseDetailResponse updateCourse(@PathVariable("id") UUID id,
                                             @Valid @RequestBody CourseRequest request,
                                             CurrentUser user) {
        return service.updateCourse(id, request, user);
    }

    @PatchMapping("/courses/{id}/publish")
    public CourseDetailResponse setPublished(@PathVariable("id") UUID id,
                                             @Valid @RequestBody PublishRequest request,
                                             CurrentUser user) {
        return service.setPublished(id, request.published(), user);
    }

    @DeleteMapping("/courses/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCourse(@PathVariable("id") UUID id, CurrentUser user) {
        service.deleteCourse(id, user);
    }

    @PostMapping("/courses/{id}/modules")
    @ResponseStatus(HttpStatus.CREATED)
    public ModuleResponse addModule(@PathVariable("id") UUID courseId,
                                    @Valid @RequestBody ModuleRequest request,
                                    CurrentUser user) {
        return service.addModule(courseId, request.title(), user);
    }

    @PutMapping("/courses/{id}/modules/order")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reorderModules(@PathVariable("id") UUID courseId,
                               @Valid @RequestBody ReorderRequest request,
                               CurrentUser user) {
        service.reorderModules(courseId, request.ids(), user);
    }

    @PutMapping("/modules/{id}")
    public ModuleResponse renameModule(@PathVariable("id") UUID moduleId,
                                       @Valid @RequestBody ModuleRequest request,
                                       CurrentUser user) {
        return service.renameModule(moduleId, request.title(), user);
    }

    @DeleteMapping("/modules/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteModule(@PathVariable("id") UUID moduleId, CurrentUser user) {
        service.deleteModule(moduleId, user);
    }
}
