package com.bd.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bd.model.Notification;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserUsernameOrderByCreatedAtDesc(String username);
    long countByUserUsernameAndIsReadFalse(String username);
}
