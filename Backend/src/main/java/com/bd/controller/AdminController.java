package com.bd.controller;

import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bd.dto.AccountDTO;
import com.bd.dto.AdminAnalyticsDTO;
import com.bd.dto.AdminTransactionDTO;
import com.bd.dto.AdminUserDTO;
import com.bd.dto.AuditLogDTO;
import com.bd.dto.RewardHistoryDTO;
import com.bd.dto.UserDetailDTO;
import com.bd.service.AdminService;
import com.bd.service.AnalyticsService;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final AdminService admin;
    private final AnalyticsService analytics;

    public AdminController(AdminService admin, AnalyticsService analytics) {
        this.admin = admin;
        this.analytics = analytics;
    }

    @GetMapping("/dashboard")
    public AdminAnalyticsDTO dashboard() {
        return analytics.adminAnalytics();
    }

    @GetMapping("/users")
    public List<AdminUserDTO> users() {
        return admin.users();
    }

    @GetMapping("/users/{id}")
    public UserDetailDTO userDetails(@PathVariable Long id) {
        return admin.getUserDetails(id);
    }

    @PatchMapping("/users/{id}/status")
    public AdminUserDTO status(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return admin.setUserStatus(id, body.getOrDefault("status", "ACTIVE"));
    }

    @PutMapping("/users/{id}")
    public AdminUserDTO updateUser(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return admin.updateUser(id, body);
    }

    @GetMapping("/transactions")
    public List<AdminTransactionDTO> transactions() {
        return admin.transactions();
    }

    @GetMapping("/rewards")
    public List<RewardHistoryDTO> rewards() {
        return admin.rewards();
    }

    @PutMapping("/accounts/{id}")
    public AccountDTO updateAccount(@PathVariable String id, @RequestBody Map<String, Object> body) {
        return admin.updateAccount(id, body);
    }

    @GetMapping("/audit-logs")
    public List<AuditLogDTO> auditLogs() {
        return admin.auditLogs();
    }
}
