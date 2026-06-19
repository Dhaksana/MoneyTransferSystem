package com.bd.service;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.bd.model.AppUser;
import com.bd.model.AuditLog;
import com.bd.repository.AppUserRepository;
import com.bd.repository.AuditLogRepository;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository auditLogRepository;
    private final AppUserRepository appUserRepository;
    private final CurrentUserService currentUserService;
    private final HttpServletRequest request;

    public AuditService(AuditLogRepository auditLogRepository,
                        AppUserRepository appUserRepository,
                        CurrentUserService currentUserService,
                        HttpServletRequest request) {
        this.auditLogRepository = auditLogRepository;
        this.appUserRepository = appUserRepository;
        this.currentUserService = currentUserService;
        this.request = request;
    }

    public void log(String username, String action, String details) {
        try {
            AppUser user = appUserRepository.findByUsername(username).orElse(null);
            AuditLog entry = new AuditLog();
            entry.setUser(user);
            entry.setAction(action);
            entry.setDetails(details);
            entry.setIpAddress(getClientIp());
            auditLogRepository.save(entry);
        } catch (Exception e) {
            log.error("Failed to write audit log: {}", e.getMessage(), e);
        }
    }

    public void log(String action, String details) {
        try {
            AppUser user = currentUserService.user();
            log(user, action, details);
        } catch (Exception e) {
            log.error("Failed to write audit log: {}", e.getMessage(), e);
        }
    }

    public void log(AppUser user, String action, String details) {
        try {
            AuditLog entry = new AuditLog();
            entry.setUser(user);
            entry.setAction(action);
            entry.setDetails(details);
            entry.setIpAddress(getClientIp());
            auditLogRepository.save(entry);
        } catch (Exception e) {
            log.error("Failed to write audit log: {}", e.getMessage(), e);
        }
    }

    private String getClientIp() {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
