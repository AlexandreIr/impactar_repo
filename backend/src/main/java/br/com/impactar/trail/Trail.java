package br.com.impactar.trail;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import br.com.impactar.shared.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

/** Trilha: sequência ordenada de cursos, gerida pela administração. */
@Entity
@Table(name = "trails")
public class Trail extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "thumbnail_url", length = 1000)
    private String thumbnailUrl;

    @Column(name = "is_published", nullable = false)
    private boolean published;

    @Column(name = "created_by", nullable = false, updatable = false)
    private UUID createdBy;

    @OneToMany(mappedBy = "trail")
    @OrderBy("orderIndex ASC")
    private List<TrailCourse> courses = new ArrayList<>();

    protected Trail() {
        // JPA
    }

    public Trail(String title, String description, String thumbnailUrl, UUID createdBy) {
        this.createdBy = createdBy;
        update(title, description, thumbnailUrl);
    }

    public void update(String title, String description, String thumbnailUrl) {
        this.title = title;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public List<TrailCourse> getCourses() {
        return courses;
    }
}
