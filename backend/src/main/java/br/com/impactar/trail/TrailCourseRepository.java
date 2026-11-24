package br.com.impactar.trail;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TrailCourseRepository extends JpaRepository<TrailCourse, UUID> {

    List<TrailCourse> findByTrail_IdOrderByOrderIndexAsc(UUID trailId);
}
