package br.com.impactar.trail;

import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TrailEnrollmentRepository extends JpaRepository<TrailEnrollment, UUID> {

    boolean existsByUserIdAndTrail_Id(UUID userId, UUID trailId);

    @Query("select te.trail.id from TrailEnrollment te where te.userId = :userId")
    Set<UUID> findTrailIdsByUserId(@Param("userId") UUID userId);
}
