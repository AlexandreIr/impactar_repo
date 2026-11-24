package br.com.impactar.certificate;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * Certificado emitido. Nome, título e carga horária são cópias (snapshot) do momento da emissão:
 * editar o curso ou o nome do usuário depois não altera certificados já emitidos.
 * Exatamente um entre courseId e trailId é preenchido (constraint no banco).
 */
@Entity
@Table(name = "certificates")
public class Certificate {

    public enum Kind { COURSE, TRAIL }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "course_id", updatable = false)
    private UUID courseId;

    @Column(name = "trail_id", updatable = false)
    private UUID trailId;

    @Column(name = "verification_code", nullable = false, updatable = false, length = 20)
    private String verificationCode;

    @Column(name = "holder_name", nullable = false, updatable = false, length = 150)
    private String holderName;

    @Column(nullable = false, updatable = false, length = 200)
    private String title;

    @Column(name = "workload_hours", updatable = false)
    private Integer workloadHours;

    @Column(name = "issued_at", nullable = false, updatable = false)
    private Instant issuedAt;

    protected Certificate() {
        // JPA
    }

    private Certificate(UUID userId, UUID courseId, UUID trailId, String verificationCode,
                        String holderName, String title, Integer workloadHours) {
        this.userId = userId;
        this.courseId = courseId;
        this.trailId = trailId;
        this.verificationCode = verificationCode;
        this.holderName = holderName;
        this.title = title;
        this.workloadHours = workloadHours;
    }

    public static Certificate forCourse(UUID userId, UUID courseId, String code,
                                        String holderName, String title, Integer workloadHours) {
        return new Certificate(userId, courseId, null, code, holderName, title, workloadHours);
    }

    public static Certificate forTrail(UUID userId, UUID trailId, String code,
                                       String holderName, String title, Integer workloadHours) {
        return new Certificate(userId, null, trailId, code, holderName, title, workloadHours);
    }

    @PrePersist
    void onCreate() {
        this.issuedAt = Instant.now();
    }

    public Kind getKind() {
        return trailId != null ? Kind.TRAIL : Kind.COURSE;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getCourseId() {
        return courseId;
    }

    public UUID getTrailId() {
        return trailId;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public String getHolderName() {
        return holderName;
    }

    public String getTitle() {
        return title;
    }

    public Integer getWorkloadHours() {
        return workloadHours;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }
}
