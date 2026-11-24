package br.com.impactar.learning;

import java.util.UUID;

import br.com.impactar.auth.CurrentUser;
import br.com.impactar.learning.dto.CompletionResponse;
import br.com.impactar.learning.dto.HomeResponse;
import br.com.impactar.learning.dto.LessonPlayerResponse;
import br.com.impactar.learning.dto.PositionRequest;
import br.com.impactar.learning.dto.StudentCourseResponse;
import br.com.impactar.learning.dto.StudentTrailResponse;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Todos os perfis (regra anyRequest no SecurityConfig).
 *
 * GET    /api/learning/home
 * GET    /api/learning/courses/{id}
 * POST   /api/learning/courses/{id}/enroll
 * GET    /api/learning/lessons/{id}
 * PUT    /api/learning/lessons/{id}/position
 * POST   /api/learning/lessons/{id}/complete
 * DELETE /api/learning/lessons/{id}/complete
 * GET    /api/learning/trails/{id}
 * POST   /api/learning/trails/{id}/enroll
 */
@RestController
@RequestMapping("/learning")
public class LearningController {

    private final LearningService service;

    public LearningController(LearningService service) {
        this.service = service;
    }

    @GetMapping("/home")
    public HomeResponse home(CurrentUser user) {
        return service.home(user);
    }

    @GetMapping("/courses/{id}")
    public StudentCourseResponse course(@PathVariable("id") UUID courseId, CurrentUser user) {
        return service.course(courseId, user);
    }

    @PostMapping("/courses/{id}/enroll")
    public StudentCourseResponse enroll(@PathVariable("id") UUID courseId, CurrentUser user) {
        return service.enroll(courseId, user);
    }

    @GetMapping("/lessons/{id}")
    public LessonPlayerResponse lesson(@PathVariable("id") UUID lessonId, CurrentUser user) {
        return service.lesson(lessonId, user);
    }

    @PutMapping("/lessons/{id}/position")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void savePosition(@PathVariable("id") UUID lessonId,
                             @Valid @RequestBody PositionRequest request,
                             CurrentUser user) {
        service.savePosition(lessonId, request.positionSeconds(), user);
    }

    @PostMapping("/lessons/{id}/complete")
    public CompletionResponse complete(@PathVariable("id") UUID lessonId, CurrentUser user) {
        return service.complete(lessonId, user);
    }

    @DeleteMapping("/lessons/{id}/complete")
    public CompletionResponse uncomplete(@PathVariable("id") UUID lessonId, CurrentUser user) {
        return service.uncomplete(lessonId, user);
    }

    @GetMapping("/trails/{id}")
    public StudentTrailResponse trail(@PathVariable("id") UUID trailId, CurrentUser user) {
        return service.trail(trailId, user);
    }

    @PostMapping("/trails/{id}/enroll")
    public StudentTrailResponse enrollTrail(@PathVariable("id") UUID trailId, CurrentUser user) {
        return service.enrollTrail(trailId, user);
    }
}
