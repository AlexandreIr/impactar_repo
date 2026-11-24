package br.com.impactar.certificate;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CertificateRepository extends JpaRepository<Certificate, UUID> {

    List<Certificate> findByUserIdOrderByIssuedAtDesc(UUID userId);

    Optional<Certificate> findByIdAndUserId(UUID id, UUID userId);

    Optional<Certificate> findByVerificationCode(String verificationCode);

    boolean existsByVerificationCode(String verificationCode);

    @Query("select c.courseId from Certificate c where c.userId = :userId and c.courseId is not null")
    Set<UUID> findCertifiedCourseIds(@Param("userId") UUID userId);

    @Query("select c.trailId from Certificate c where c.userId = :userId and c.trailId is not null")
    Set<UUID> findCertifiedTrailIds(@Param("userId") UUID userId);
}
