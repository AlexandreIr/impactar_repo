package br.com.impactar.course;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/** Material complementar (link do Google Drive no MVP). Tabela sem updated_at. */
@Entity
@Table(name = "lesson_materials")
public class LessonMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 1000)
    private String url;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected LessonMaterial() {
        // JPA
    }

    public LessonMaterial(Lesson lesson, String title, String url, int orderIndex) {
        this.lesson = lesson;
        this.title = title;
        this.url = url;
        this.orderIndex = orderIndex;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public Lesson getLesson() {
        return lesson;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public int getOrderIndex() {
        return orderIndex;
    }
}
