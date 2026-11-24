package br.com.impactar.trail;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import br.com.impactar.auth.CurrentUser;
import br.com.impactar.course.Course;
import br.com.impactar.course.CourseRepository;
import br.com.impactar.shared.ApiException;
import br.com.impactar.shared.Text;
import br.com.impactar.trail.dto.TrailAdminDetailResponse;
import br.com.impactar.trail.dto.TrailAdminSummaryResponse;
import br.com.impactar.trail.dto.TrailRequest;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Gestão de trilhas (somente administradores). */
@Service
public class TrailAdminService {

    private final TrailRepository trailRepository;
    private final TrailCourseRepository trailCourseRepository;
    private final CourseRepository courseRepository;

    public TrailAdminService(TrailRepository trailRepository,
                             TrailCourseRepository trailCourseRepository,
                             CourseRepository courseRepository) {
        this.trailRepository = trailRepository;
        this.trailCourseRepository = trailCourseRepository;
        this.courseRepository = courseRepository;
    }

    @Transactional(readOnly = true)
    public List<TrailAdminSummaryResponse> list() {
        return trailRepository.findAllByOrderByUpdatedAtDesc().stream()
                .map(TrailAdminSummaryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public TrailAdminDetailResponse get(UUID trailId) {
        return TrailAdminDetailResponse.from(findTrail(trailId));
    }

    @Transactional
    public TrailAdminDetailResponse create(TrailRequest request, CurrentUser admin) {
        Trail trail = trailRepository.save(new Trail(
                request.title().trim(),
                Text.trimToNull(request.description()),
                Text.trimToNull(request.thumbnailUrl()),
                admin.id()));
        return TrailAdminDetailResponse.from(trail);
    }

    @Transactional
    public TrailAdminDetailResponse update(UUID trailId, TrailRequest request) {
        Trail trail = findTrail(trailId);
        trail.update(
                request.title().trim(),
                Text.trimToNull(request.description()),
                Text.trimToNull(request.thumbnailUrl()));
        return TrailAdminDetailResponse.from(trail);
    }

    @Transactional
    public TrailAdminDetailResponse setPublished(UUID trailId, boolean published) {
        Trail trail = findTrail(trailId);
        if (published && trail.getCourses().stream().noneMatch(tc -> tc.getCourse().isPublished())) {
            throw ApiException.badRequest("Para publicar, a trilha precisa de pelo menos um curso publicado.");
        }
        trail.setPublished(published);
        return TrailAdminDetailResponse.from(trail);
    }

    @Transactional
    public void delete(UUID trailId) {
        Trail trail = findTrail(trailId);
        if (trail.isPublished()) {
            throw ApiException.badRequest("Despublique a trilha antes de excluí-la.");
        }
        trailRepository.delete(trail);
        trailRepository.flush();
    }

    /**
     * Substitui os cursos da trilha pela lista informada (na ordem).
     * Atualiza por diferença: remove os que saíram, reordena os que ficaram, insere os novos.
     */
    @Transactional
    public TrailAdminDetailResponse setCourses(UUID trailId, List<UUID> courseIds) {
        Trail trail = findTrail(trailId);
        if (new HashSet<>(courseIds).size() != courseIds.size()) {
            throw ApiException.badRequest("Um curso não pode aparecer duas vezes na mesma trilha.");
        }

        List<TrailCourse> current = trailCourseRepository.findByTrail_IdOrderByOrderIndexAsc(trailId);
        Map<UUID, TrailCourse> byCourseId = current.stream()
                .collect(Collectors.toMap(tc -> tc.getCourse().getId(), Function.identity()));

        for (TrailCourse trailCourse : current) {
            if (!courseIds.contains(trailCourse.getCourse().getId())) {
                trailCourseRepository.delete(trailCourse);
            }
        }

        for (int index = 0; index < courseIds.size(); index++) {
            UUID courseId = courseIds.get(index);
            TrailCourse existing = byCourseId.get(courseId);
            if (existing != null) {
                existing.setOrderIndex(index);
            } else {
                Course course = courseRepository.findById(courseId)
                        .orElseThrow(() -> ApiException.badRequest("Curso não encontrado. Recarregue a página."));
                trailCourseRepository.save(new TrailCourse(trail, course, index));
            }
        }

        trailCourseRepository.flush();
        if (trail.isPublished() && courseIds.isEmpty()) {
            throw ApiException.badRequest("Uma trilha publicada precisa de pelo menos um curso.");
        }
        return TrailAdminDetailResponse.from(trail);
    }

    private Trail findTrail(UUID trailId) {
        return trailRepository.findById(trailId)
                .orElseThrow(() -> ApiException.notFound("Trilha não encontrada."));
    }
}
