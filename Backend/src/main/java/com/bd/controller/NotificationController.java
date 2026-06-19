package com.bd.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bd.dto.NotificationDTO;
import com.bd.service.CurrentUserService;
import com.bd.service.NotificationService;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    private final NotificationService notifications;
    private final CurrentUserService currentUser;

    public NotificationController(NotificationService notifications, CurrentUserService currentUser) {
        this.notifications = notifications;
        this.currentUser = currentUser;
    }

    @GetMapping
    public List<NotificationDTO> list() {
        return notifications.list(currentUser.username());
    }

    @GetMapping("/unread-count")
    public Map<String, Long> unreadCount() {
        return Map.of("count", notifications.unreadCount(currentUser.username()));
    }

    @PatchMapping("/{id}/read")
    public NotificationDTO markRead(@PathVariable Long id) {
        return notifications.markRead(id, currentUser.username());
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        notifications.delete(id, currentUser.username());
    }
}
