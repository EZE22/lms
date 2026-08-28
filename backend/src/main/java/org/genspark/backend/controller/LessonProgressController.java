package org.genspark.backend.controller;

import org.genspark.backend.entity.LessonProgress;
import org.genspark.backend.service.LessonProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/progress")
public class LessonProgressController {

    private final LessonProgressService lessonProgressService;

    @Autowired
    public LessonProgressController(LessonProgressService lessonProgressService) {
        this.lessonProgressService = lessonProgressService;
    }

    @PostMapping
    public LessonProgress markComplete(@RequestParam Long userId, @RequestParam Long lessonId) {
        return lessonProgressService.markComplete(userId, lessonId);
    }

    @GetMapping("/user/{userId}")
    public List<LessonProgress> getProgressForUser(@PathVariable Long userId) {
        return lessonProgressService.getProgressForUser(userId);
    }
}
