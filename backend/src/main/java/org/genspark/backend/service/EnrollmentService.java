package org.genspark.backend.service;

import org.genspark.backend.entity.Course;
import org.genspark.backend.entity.Enrollment;
import org.genspark.backend.entity.User;
import org.genspark.backend.repository.EnrollmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final UserService userService;
    private final CourseService courseService;

    @Autowired
    public EnrollmentService(EnrollmentRepository enrollmentRepository, UserService userService, CourseService courseService) {
        this.enrollmentRepository = enrollmentRepository;
        this.userService = userService;
        this.courseService = courseService;
    }

    public Enrollment enroll(Long userId, Long courseId) {
        if (enrollmentRepository.existsByUserIdAndCourseId(userId, courseId)) {
            throw new RuntimeException("User is already enrolled in this course");
        }
        User user = userService.getUserById(userId);
        Course course = courseService.getCourseById(courseId);
        return enrollmentRepository.save(new Enrollment(user, course));
    }

    public List<Enrollment> getEnrollmentsForUser(Long userId) {
        return enrollmentRepository.findByUserId(userId);
    }
}
