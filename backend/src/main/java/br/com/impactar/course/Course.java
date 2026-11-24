package br.com.impactar.course;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import br.com.impactar.shared.BaseEntity;
import br.com.impactar.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

/**
 * Coleções filhas são somente leitura (mappedBy, sem cascade):
 * filhos são criados pelos próprios repositórios e excluídos pelo ON DELETE CASCADE do banco.
 */
@Entity
@Table(name = "courses")
public class Course extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instructor_id", nullable = false)
    private User instructor;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "thumbnail_url", length = 1000)
    private String thumbnailUrl;

    @Column(name = "is_published", nullable = false)
    private boolean published;

    @Column(name = "workload_hours")
    private Integer workloadHours;

    @OneToMany(mappedBy = "course")
    @OrderBy("orderIndex ASC")
    private List<CourseModule> modules = new ArrayList<>();

    protected Course() {
        // JPA
    }

    public Course(User instructor, String title, String description, String thumbnailUrl, Integer workloadHours) {
        this.instructor = instructor;
        update(title, description, thumbnailUrl, workloadHours);
    }

    public void update(String title, String description, String thumbnailUrl, Integer workloadHours) {
        this.title = title;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.workloadHours = workloadHours;
    }

    public boolean isOwnedBy(UUID userId) {
        return instructor.getId().equals(userId);
    }

    public User getInstructor() {
        return instructor;
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

    public Integer getWorkloadHours() {
        return workloadHours;
    }

    public List<CourseModule> getModules() {
        return modules;
    }
}
