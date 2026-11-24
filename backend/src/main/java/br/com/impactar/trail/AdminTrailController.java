package br.com.impactar.trail;

import java.util.List;
import java.util.UUID;

import br.com.impactar.auth.CurrentUser;
import br.com.impactar.course.dto.PublishRequest;
import br.com.impactar.trail.dto.TrailAdminDetailResponse;
import br.com.impactar.trail.dto.TrailAdminSummaryResponse;
import br.com.impactar.trail.dto.TrailCoursesRequest;
import br.com.impactar.trail.dto.TrailRequest;
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
 * Somente ADMIN (regra /admin/**).
 *
 * GET    /api/admin/trails
 * POST   /api/admin/trails
 * GET    /api/admin/trails/{id}
 * PUT    /api/admin/trails/{id}
 * PATCH  /api/admin/trails/{id}/publish
 * DELETE /api/admin/trails/{id}
 * PUT    /api/admin/trails/{id}/courses   -> lista completa, na ordem
 */
@RestController
@RequestMapping("/admin/trails")
public class AdminTrailController {

    private final TrailAdminService service;

    public AdminTrailController(TrailAdminService service) {
        this.service = service;
    }

    @GetMapping
    public List<TrailAdminSummaryResponse> list() {
        return service.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TrailAdminDetailResponse create(@Valid @RequestBody TrailRequest request, CurrentUser user) {
        return service.create(request, user);
    }

    @GetMapping("/{id}")
    public TrailAdminDetailResponse get(@PathVariable("id") UUID id) {
        return service.get(id);
    }

    @PutMapping("/{id}")
    public TrailAdminDetailResponse update(@PathVariable("id") UUID id, @Valid @RequestBody TrailRequest request) {
        return service.update(id, request);
    }

    @PatchMapping("/{id}/publish")
    public TrailAdminDetailResponse setPublished(@PathVariable("id") UUID id,
                                                 @Valid @RequestBody PublishRequest request) {
        return service.setPublished(id, request.published());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") UUID id) {
        service.delete(id);
    }

    @PutMapping("/{id}/courses")
    public TrailAdminDetailResponse setCourses(@PathVariable("id") UUID id,
                                               @Valid @RequestBody TrailCoursesRequest request) {
        return service.setCourses(id, request.courseIds());
    }
}
