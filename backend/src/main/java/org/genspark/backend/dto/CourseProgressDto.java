package org.genspark.backend.dto;

public class CourseProgressDto {
    private Long courseId;
    private String courseTitle;
    private int totalLessons;
    private int completedLessons;

    public CourseProgressDto(Long courseId, String courseTitle, int totalLessons, int completedLessons) {
        this.courseId = courseId;
        this.courseTitle = courseTitle;
        this.totalLessons = totalLessons;
        this.completedLessons = completedLessons;
    }

    public Long getCourseId() { return courseId; }
    public String getCourseTitle() { return courseTitle; }
    public int getTotalLessons() { return totalLessons; }
    public int getCompletedLessons() { return completedLessons; }
}