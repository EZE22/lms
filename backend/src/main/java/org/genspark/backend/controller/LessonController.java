package org.genspark.backend.controller;

import org.genspark.backend.entity.Lesson;
import org.genspark.backend.service.LessonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses/{courseId}/lessons")
public class LessonController {

    private final LessonService lessonService;

    @Autowired
    public LessonController(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    @PostMapping
    public Lesson createLesson(@PathVariable Long courseId, @RequestBody Lesson lesson) {
        return lessonService.createLesson(courseId, lesson);
    }

    @GetMapping
    public List<Lesson> getLessonsForCourse(@PathVariable Long courseId) {
        return lessonService.getLessonsForCourse(courseId);
    }
}