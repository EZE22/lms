package org.genspark.backend.dto;

import java.util.List;

public class UserDashboardDto {
    private Long userId;
    private String userName;
    private List<CourseProgressDto> courses;

    public UserDashboardDto(Long userId, String userName, List<CourseProgressDto> courses) {
        this.userId = userId;
        this.userName = userName;
        this.courses = courses;
    }

    public Long getUserId() { return userId; }
    public String getUserName() { return userName; }
    public List<CourseProgressDto> getCourses() { return courses; }
}