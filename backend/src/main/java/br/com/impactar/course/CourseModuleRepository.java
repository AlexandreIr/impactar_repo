package br.com.impactar.course;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseModuleRepository extends JpaRepository<CourseModule, UUID> {

    List<CourseModule> findByCourse_IdOrderByOrderIndexAsc(UUID courseId);

    @Query("select coalesce(max(m.orderIndex), -1) from CourseModule m where m.course.id = :courseId")
    int findMaxOrderIndex(@Param("courseId") UUID courseId);
}
