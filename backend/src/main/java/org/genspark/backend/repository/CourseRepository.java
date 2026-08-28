package org.genspark.backend.repository;

import org.genspark.backend.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {
}

/*
save(Course course) — insert or update
findById(Long id) — returns Optional<Course>
findAll() — returns List<Course>
deleteById(Long id)
count(), existsById(), and more
 */