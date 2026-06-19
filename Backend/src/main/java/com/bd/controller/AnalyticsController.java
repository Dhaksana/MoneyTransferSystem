package com.bd.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bd.dto.AdminAnalyticsDTO;
import com.bd.dto.AnalyticsDTO;
import com.bd.service.AnalyticsService;
import com.bd.service.CurrentUserService;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {
    private final AnalyticsService analytics;
    private final CurrentUserService currentUser;

    public AnalyticsController(AnalyticsService analytics, CurrentUserService currentUser) {
        this.analytics = analytics;
        this.currentUser = currentUser;
    }

    @GetMapping("/me")
    public AnalyticsDTO mine() {
        return analytics.userAnalytics(currentUser.username());
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminAnalyticsDTO admin() {
        return analytics.adminAnalytics();
    }
}
