package org.genspark.backend.controller;

import org.genspark.backend.dto.CourseProgressDto;
import org.genspark.backend.dto.UserDashboardDto;
import org.genspark.backend.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    @Autowired
    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/{userId}")
    public UserDashboardDto getDashboard(@PathVariable Long userId) {
        return dashboardService.getDashboard(userId);
    }
}