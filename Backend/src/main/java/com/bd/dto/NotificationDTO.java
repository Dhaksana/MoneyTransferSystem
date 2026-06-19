package com.bd.dto;

import java.time.LocalDateTime;

import com.bd.model.Notification;

public record NotificationDTO(Long id, String message, String type, boolean read, LocalDateTime createdAt) {
    public static NotificationDTO from(Notification notification) {
        return new NotificationDTO(
                notification.getId(),
                notification.getMessage(),
                notification.getType(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
