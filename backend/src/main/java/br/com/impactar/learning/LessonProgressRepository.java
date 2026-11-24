package br.com.impactar.learning;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.impactar.course.CourseLessonCount;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LessonProgressRepository extends JpaRepository<LessonProgress, UUID> {

    Optional<LessonProgress> findByUserIdAndLesson_Id(UUID userId, UUID lessonId);

    /** Atividade mais recente do aluno (card "Continuar de onde parou"). */
    List<LessonProgress> findTop10ByUserIdOrderByUpdatedAtDesc(UUID userId);

    @Query("""
            select p from LessonProgress p
            join p.lesson l join l.module m
            where p.userId = :userId and m.course.id = :courseId
            """)
    List<LessonProgress> findByUserIdAndCourseId(@Param("userId") UUID userId, @Param("courseId") UUID courseId);

    @Query("""
            select new br.com.impactar.course.CourseLessonCount(m.course.id, count(p))
            from LessonProgress p join p.lesson l join l.module m
            where p.userId = :userId and p.completedAt is not null and m.course.id in :courseIds
            group by m.course.id
            """)
    List<CourseLessonCount> countCompletedByCourse(@Param("userId") UUID userId,
                                                   @Param("courseIds") Collection<UUID> courseIds);
}
