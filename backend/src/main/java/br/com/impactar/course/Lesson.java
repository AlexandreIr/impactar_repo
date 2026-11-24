package br.com.impactar.course;

import java.util.ArrayList;
import java.util.List;

import br.com.impactar.shared.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

@Entity
@Table(name = "lessons")
public class Lesson extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "module_id", nullable = false)
    private CourseModule module;

    @Column(nullable = false, length = 200)
    private String title;

    /** Texto complementar; links são exibidos clicáveis para o aluno. */
    @Column(columnDefinition = "text")
    private String description;

    /** URL canônica do YouTube (https://www.youtube.com/watch?v=ID) ou null. */
    @Column(name = "video_url", length = 1000)
    private String videoUrl;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    @OneToMany(mappedBy = "lesson")
    @OrderBy("orderIndex ASC")
    private List<LessonMaterial> materials = new ArrayList<>();

    protected Lesson() {
        // JPA
    }

    public Lesson(CourseModule module, String title, String description, String videoUrl, int orderIndex) {
        this.module = module;
        this.orderIndex = orderIndex;
        update(title, description, videoUrl);
    }

    public void update(String title, String description, String videoUrl) {
        this.title = title;
        this.description = description;
        this.videoUrl = videoUrl;
    }

    public CourseModule getModule() {
        return module;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    public List<LessonMaterial> getMaterials() {
        return materials;
    }
}
