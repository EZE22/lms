package org.genspark.backend.service;

import org.genspark.backend.dto.CourseProgressDto;
import org.genspark.backend.dto.UserDashboardDto;
import org.genspark.backend.entity.*;
import org.genspark.backend.repository.EnrollmentRepository;
import org.genspark.backend.repository.LessonProgressRepository;
import org.genspark.backend.repository.LessonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final EnrollmentRepository enrollmentRepository;
    private final LessonRepository lessonRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final UserService userService;

    @Autowired
    public DashboardService(EnrollmentRepository enrollmentRepository,
                            LessonRepository lessonRepository,
                            LessonProgressRepository lessonProgressRepository,
                            UserService userService) {
        this.enrollmentRepository = enrollmentRepository;
        this.lessonRepository = lessonRepository;
        this.lessonProgressRepository = lessonProgressRepository;
        this.userService = userService;
    }

    public UserDashboardDto getDashboard(Long userId) {
        User user = userService.getUserById(userId);

        List<Enrollment> enrollments = enrollmentRepository.findByUserId(userId);
        List<LessonProgress> allProgress = lessonProgressRepository.findByUserId(userId);

        Set<Long> completedLessonIds = allProgress.stream()
                .filter(LessonProgress::isCompleted)
                .map(lp -> lp.getLesson().getId())
                .collect(Collectors.toSet());

        List<CourseProgressDto> courseProgress = enrollments.stream()
                .map(enrollment -> {
                    Long courseId = enrollment.getCourse().getId();
                    String courseTitle = enrollment.getCourse().getTitle();

                    List<Lesson> lessonsInCourse =
                            lessonRepository.findByCourseIdOrderByOrderIndexAsc(courseId);

                    int totalLessons = lessonsInCourse.size();

                    int completedCount = (int) lessonsInCourse.stream()
                            .filter(lesson -> completedLessonIds.contains(lesson.getId()))
                            .count();

                    return new CourseProgressDto(courseId, courseTitle, totalLessons, completedCount);
                })
                .collect(Collectors.toList());

        return new UserDashboardDto(user.getId(), user.getName(), courseProgress);
    }
}