package br.com.impactar.course;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LessonRepository extends JpaRepository<Lesson, UUID> {

    List<Lesson> findByModule_IdOrderByOrderIndexAsc(UUID moduleId);

    long countByModule_Course_IdAndVideoUrlIsNotNull(UUID courseId);

    @Query("select coalesce(max(l.orderIndex), -1) from Lesson l where l.module.id = :moduleId")
    int findMaxOrderIndex(@Param("moduleId") UUID moduleId);

    /** Total de aulas por curso (cálculo de progresso). */
    @Query("""
            select new br.com.impactar.course.CourseLessonCount(m.course.id, count(l))
            from Lesson l join l.module m
            where m.course.id in :courseIds
            group by m.course.id
            """)
    List<CourseLessonCount> countLessonsByCourse(@Param("courseIds") Collection<UUID> courseIds);
}
