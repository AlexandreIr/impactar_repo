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

/** Tabela `modules`. Nome Java evita conflito com java.lang.Module. */
@Entity
@Table(name = "modules")
public class CourseModule extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    @OneToMany(mappedBy = "module")
    @OrderBy("orderIndex ASC")
    private List<Lesson> lessons = new ArrayList<>();

    protected CourseModule() {
        // JPA
    }

    public CourseModule(Course course, String title, int orderIndex) {
        this.course = course;
        this.title = title;
        this.orderIndex = orderIndex;
    }

    public Course getCourse() {
        return course;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    public List<Lesson> getLessons() {
        return lessons;
    }
}
