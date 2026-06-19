package com.bd.repository;

import com.bd.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findAllByOrderByTimestampDesc();

    Page<AuditLog> findByActionContainingIgnoreCaseAndUserUsernameContainingIgnoreCaseAndTimestampBetween(String action, String username, LocalDateTime from, LocalDateTime to, Pageable pageable);

    Page<AuditLog> findByActionContainingIgnoreCaseAndTimestampBetween(String action, LocalDateTime from, LocalDateTime to, Pageable pageable);

    Page<AuditLog> findByUserUsernameContainingIgnoreCaseAndTimestampBetween(String username, LocalDateTime from, LocalDateTime to, Pageable pageable);

    Page<AuditLog> findByTimestampBetween(LocalDateTime from, LocalDateTime to, Pageable pageable);

    Page<AuditLog> findByActionContainingIgnoreCase(String action, Pageable pageable);

    Page<AuditLog> findByUserUsernameContainingIgnoreCase(String username, Pageable pageable);

    long countByActionContainingIgnoreCaseAndUserUsernameContainingIgnoreCaseAndTimestampBetween(String action, String username, LocalDateTime from, LocalDateTime to);

    long countByActionContainingIgnoreCaseAndTimestampBetween(String action, LocalDateTime from, LocalDateTime to);

    long countByUserUsernameContainingIgnoreCaseAndTimestampBetween(String username, LocalDateTime from, LocalDateTime to);

    long countByTimestampBetween(LocalDateTime from, LocalDateTime to);

    long countByActionContainingIgnoreCase(String action);

    long countByUserUsernameContainingIgnoreCase(String username);

    List<AuditLog> findByActionContainingIgnoreCaseAndUserUsernameContainingIgnoreCaseAndTimestampBetweenOrderByTimestampDesc(String action, String username, LocalDateTime from, LocalDateTime to);

    List<AuditLog> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime from, LocalDateTime to);

    List<AuditLog> findByActionContainingIgnoreCaseAndTimestampBetweenOrderByTimestampDesc(String action, LocalDateTime from, LocalDateTime to);

    List<AuditLog> findByUserUsernameContainingIgnoreCaseAndTimestampBetweenOrderByTimestampDesc(String username, LocalDateTime from, LocalDateTime to);

    List<AuditLog> findByActionContainingIgnoreCaseOrderByTimestampDesc(String action);

    List<AuditLog> findByUserUsernameContainingIgnoreCaseOrderByTimestampDesc(String username);
}
