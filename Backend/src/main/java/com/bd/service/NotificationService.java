package com.bd.service;

import java.util.List;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bd.dto.NotificationDTO;
import com.bd.model.AppUser;
import com.bd.model.Notification;
import com.bd.repository.AppUserRepository;
import com.bd.repository.NotificationRepository;

@Service
public class NotificationService {
    private final NotificationRepository notifications;
    private final AppUserRepository users;
    private final SimpMessagingTemplate messaging;

    public NotificationService(NotificationRepository notifications, AppUserRepository users, SimpMessagingTemplate messaging) {
        this.notifications = notifications;
        this.users = users;
        this.messaging = messaging;
    }

    public NotificationDTO create(AppUser user, String type, String message) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setMessage(message);
        Notification saved = notifications.save(notification);
        NotificationDTO dto = NotificationDTO.from(saved);
        messaging.convertAndSendToUser(user.getUsername(), "/queue/notifications", dto);
        return dto;
    }

    public List<NotificationDTO> list(String username) {
        return notifications.findByUserUsernameOrderByCreatedAtDesc(username).stream().map(NotificationDTO::from).toList();
    }

    public long unreadCount(String username) {
        return notifications.countByUserUsernameAndIsReadFalse(username);
    }

    @Transactional
    public NotificationDTO markRead(Long id, String username) {
        Notification notification = owned(id, username);
        notification.setRead(true);
        return NotificationDTO.from(notification);
    }

    @Transactional
    public void delete(Long id, String username) {
        notifications.delete(owned(id, username));
    }

    private Notification owned(Long id, String username) {
        Notification notification = notifications.findById(id).orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        if (!username.equals(notification.getUser().getUsername())) {
            throw new IllegalArgumentException("Cannot modify another user's notification");
        }
        return notification;
    }
}
