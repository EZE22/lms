package org.genspark.backend.controller;

import org.genspark.backend.entity.Enrollment;
import org.genspark.backend.service.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @Autowired
    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping
    public Enrollment enroll(@RequestParam Long userId, @RequestParam Long courseId) {
        return enrollmentService.enroll(userId, courseId);
    }

    @GetMapping("/user/{userId}")
    public List<Enrollment> getEnrollmentsForUser(@PathVariable Long userId) {
        return enrollmentService.getEnrollmentsForUser(userId);
    }
}
