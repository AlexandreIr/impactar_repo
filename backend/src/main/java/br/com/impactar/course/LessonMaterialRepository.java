package br.com.impactar.course;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LessonMaterialRepository extends JpaRepository<LessonMaterial, UUID> {

    @Query("select coalesce(max(m.orderIndex), -1) from LessonMaterial m where m.lesson.id = :lessonId")
    int findMaxOrderIndex(@Param("lessonId") UUID lessonId);
}
