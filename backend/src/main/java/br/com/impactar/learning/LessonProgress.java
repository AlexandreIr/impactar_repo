package br.com.impactar.learning;

import java.time.Instant;
import java.util.UUID;

import br.com.impactar.course.Lesson;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Progresso do aluno em uma aula.
 * Linha criada no primeiro acesso; completed_at preenchido ao concluir;
 * updated_at indica a última atividade (base do "continuar de onde parou").
 */
@Entity
@Table(name = "lesson_progress")
public class LessonProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "last_position_seconds", nullable = false)
    private int lastPositionSeconds;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected LessonProgress() {
        // JPA
    }

    public LessonProgress(UUID userId, Lesson lesson) {
        this.userId = userId;
        this.lesson = lesson;
        this.updatedAt = Instant.now();
    }

    /** Registra a posição do vídeo e marca a aula como a atividade mais recente. */
    public void updatePosition(int positionSeconds) {
        this.lastPositionSeconds = Math.max(0, positionSeconds);
        this.updatedAt = Instant.now();
    }

    public void complete() {
        if (completedAt == null) {
            completedAt = Instant.now();
        }
        this.updatedAt = Instant.now();
    }

    public void uncomplete() {
        this.completedAt = null;
        this.updatedAt = Instant.now();
    }

    public boolean isCompleted() {
        return completedAt != null;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public Lesson getLesson() {
        return lesson;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public int getLastPositionSeconds() {
        return lastPositionSeconds;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
