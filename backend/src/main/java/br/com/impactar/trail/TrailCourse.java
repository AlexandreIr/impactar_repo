package br.com.impactar.trail;

import java.util.UUID;

import br.com.impactar.course.Course;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "trail_courses")
public class TrailCourse {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trail_id", nullable = false)
    private Trail trail;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    protected TrailCourse() {
        // JPA
    }

    public TrailCourse(Trail trail, Course course, int orderIndex) {
        this.trail = trail;
        this.course = course;
        this.orderIndex = orderIndex;
    }

    public UUID getId() {
        return id;
    }

    public Trail getTrail() {
        return trail;
    }

    public Course getCourse() {
        return course;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }
}
