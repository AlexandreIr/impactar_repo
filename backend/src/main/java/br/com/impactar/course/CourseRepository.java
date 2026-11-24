package br.com.impactar.course;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, UUID> {

    List<Course> findByInstructor_IdOrderByUpdatedAtDesc(UUID instructorId);

    List<Course> findAllByOrderByUpdatedAtDesc();
}
