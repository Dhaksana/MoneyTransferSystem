package com.bd.dto;

import java.time.LocalDateTime;

import com.bd.model.AuditLog;

public record AuditLogDTO(Long id, String username, String action, String details, String ipAddress, LocalDateTime timestamp) {
    public static AuditLogDTO from(AuditLog log) {
        return new AuditLogDTO(
                log.getId(),
                log.getUser() == null ? null : log.getUser().getUsername(),
                log.getAction(),
                log.getDetails(),
                log.getIpAddress(),
                log.getTimestamp()
        );
    }
}
