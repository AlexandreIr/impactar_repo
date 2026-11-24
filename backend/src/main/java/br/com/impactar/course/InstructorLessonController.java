package br.com.impactar.course;

import java.util.UUID;

import br.com.impactar.auth.CurrentUser;
import br.com.impactar.course.dto.LessonRequest;
import br.com.impactar.course.dto.LessonResponse;
import br.com.impactar.course.dto.MaterialRequest;
import br.com.impactar.course.dto.MaterialResponse;
import br.com.impactar.course.dto.ReorderRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * POST   /api/instructor/modules/{id}/lessons
 * PUT    /api/instructor/modules/{id}/lessons/order
 * PUT    /api/instructor/lessons/{id}
 * DELETE /api/instructor/lessons/{id}
 * POST   /api/instructor/lessons/{id}/materials
 * DELETE /api/instructor/materials/{id}
 */
@RestController
@RequestMapping("/instructor")
public class InstructorLessonController {

    private final LessonEditorService service;

    public InstructorLessonController(LessonEditorService service) {
        this.service = service;
    }

    @PostMapping("/modules/{id}/lessons")
    @ResponseStatus(HttpStatus.CREATED)
    public LessonResponse addLesson(@PathVariable("id") UUID moduleId,
                                    @Valid @RequestBody LessonRequest request,
                                    CurrentUser user) {
        return service.addLesson(moduleId, request, user);
    }

    @PutMapping("/modules/{id}/lessons/order")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reorderLessons(@PathVariable("id") UUID moduleId,
                               @Valid @RequestBody ReorderRequest request,
                               CurrentUser user) {
        service.reorderLessons(moduleId, request.ids(), user);
    }

    @PutMapping("/lessons/{id}")
    public LessonResponse updateLesson(@PathVariable("id") UUID lessonId,
                                       @Valid @RequestBody LessonRequest request,
                                       CurrentUser user) {
        return service.updateLesson(lessonId, request, user);
    }

    @DeleteMapping("/lessons/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLesson(@PathVariable("id") UUID lessonId, CurrentUser user) {
        service.deleteLesson(lessonId, user);
    }

    @PostMapping("/lessons/{id}/materials")
    @ResponseStatus(HttpStatus.CREATED)
    public MaterialResponse addMaterial(@PathVariable("id") UUID lessonId,
                                        @Valid @RequestBody MaterialRequest request,
                                        CurrentUser user) {
        return service.addMaterial(lessonId, request, user);
    }

    @DeleteMapping("/materials/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMaterial(@PathVariable("id") UUID materialId, CurrentUser user) {
        service.deleteMaterial(materialId, user);
    }
}
