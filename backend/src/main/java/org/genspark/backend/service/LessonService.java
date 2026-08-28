package org.genspark.backend.service;

import org.genspark.backend.entity.Course;
import org.genspark.backend.entity.Lesson;
import org.genspark.backend.repository.LessonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LessonService {

    private final LessonRepository lessonRepository;
    private final CourseService courseService;

    @Autowired
    public LessonService(LessonRepository lessonRepository, CourseService courseService) {
        this.lessonRepository = lessonRepository;
        this.courseService = courseService;
    }

    public Lesson createLesson(Long courseId, Lesson lesson) {
        Course course = courseService.getCourseById(courseId);
        lesson.setCourse(course);
        return lessonRepository.save(lesson);
    }

    public List<Lesson> getLessonsForCourse(Long courseId) {
        return lessonRepository.findByCourseIdOrderByOrderIndexAsc(courseId);
    }
}