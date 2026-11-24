package br.com.impactar.learning;

import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {

    boolean existsByUserIdAndCourse_Id(UUID userId, UUID courseId);

    @Query("select e.course.id from Enrollment e where e.userId = :userId")
    Set<UUID> findCourseIdsByUserId(@Param("userId") UUID userId);
}
