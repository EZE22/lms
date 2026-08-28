package org.genspark.backend.service;

import org.genspark.backend.entity.Lesson;
import org.genspark.backend.entity.LessonProgress;
import org.genspark.backend.entity.User;
import org.genspark.backend.repository.LessonProgressRepository;
import org.genspark.backend.repository.LessonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LessonProgressService {

    private final LessonProgressRepository lessonProgressRepository;
    private final UserService userService;
    private final LessonRepository lessonRepository;

    @Autowired
    public LessonProgressService(LessonProgressRepository lessonProgressRepository, UserService userService, LessonRepository lessonRepository) {
        this.lessonProgressRepository = lessonProgressRepository;
        this.userService = userService;
        this.lessonRepository = lessonRepository;
    }

    public LessonProgress markComplete(Long userId, Long lessonId) {
        return lessonProgressRepository.findByUserIdAndLessonId(userId, lessonId)
                .map(progress -> {
                    progress.setCompleted(true);
                    progress.setCompletedAt(LocalDateTime.now());
                    return lessonProgressRepository.save(progress);
                })
                .orElseGet(() -> {
                    User user = userService.getUserById(userId);
                    Lesson lesson = lessonRepository.findById(lessonId)
                            .orElseThrow(() -> new RuntimeException("Lesson not found with id: " + lessonId));
                    LessonProgress progress = new LessonProgress(user, lesson);
                    progress.setCompleted(true);
                    progress.setCompletedAt(LocalDateTime.now());
                    return lessonProgressRepository.save(progress);
                });
    }

    public List<LessonProgress> getProgressForUser(Long userId) {
        return lessonProgressRepository.findByUserId(userId);
    }
}
