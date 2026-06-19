package com.bd.controller;

import com.bd.dto.AuditLogDTO;
import com.bd.repository.AuditLogRepository;
import com.bd.service.AuditService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/audit-logs")
@PreAuthorize("hasRole('ADMIN')")
public class AuditController {
    private final AuditLogRepository auditLogRepository;
    private final AuditService auditService;

    public AuditController(AuditLogRepository auditLogRepository, AuditService auditService) {
        this.auditLogRepository = auditLogRepository;
        this.auditService = auditService;
    }

    @GetMapping("/search")
    public Page<AuditLogDTO> search(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size);
        LocalDateTime toDate = to != null ? LocalDateTime.parse(to) : LocalDateTime.now();
        LocalDateTime fromDate;
        if (from != null) {
            fromDate = LocalDateTime.parse(from);
        } else if (to != null) {
            fromDate = toDate.minusDays(30);
        } else {
            fromDate = null;
        }

        if (action != null && username != null && fromDate != null) {
            return auditLogRepository.findByActionContainingIgnoreCaseAndUserUsernameContainingIgnoreCaseAndTimestampBetween(action, username, fromDate, toDate, pageable).map(AuditLogDTO::from);
        }
        if (action != null && fromDate != null) {
            return auditLogRepository.findByActionContainingIgnoreCaseAndTimestampBetween(action, fromDate, toDate, pageable).map(AuditLogDTO::from);
        }
        if (username != null && fromDate != null) {
            return auditLogRepository.findByUserUsernameContainingIgnoreCaseAndTimestampBetween(username, fromDate, toDate, pageable).map(AuditLogDTO::from);
        }
        if (fromDate != null) {
            return auditLogRepository.findByTimestampBetween(fromDate, toDate, pageable).map(AuditLogDTO::from);
        }
        if (action != null) {
            return auditLogRepository.findByActionContainingIgnoreCase(action, pageable).map(AuditLogDTO::from);
        }
        if (username != null) {
            return auditLogRepository.findByUserUsernameContainingIgnoreCase(username, pageable).map(AuditLogDTO::from);
        }
        return auditLogRepository.findAll(pageable).map(AuditLogDTO::from);
    }

    @GetMapping("/export")
    public List<AuditLogDTO> export(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        LocalDateTime toDate = to != null ? LocalDateTime.parse(to) : LocalDateTime.now();
        LocalDateTime fromDate;
        if (from != null) {
            fromDate = LocalDateTime.parse(from);
        } else if (to != null) {
            fromDate = toDate.minusDays(30);
        } else {
            fromDate = null;
        }

        if (action != null && username != null && fromDate != null) {
            return auditLogRepository.findByActionContainingIgnoreCaseAndUserUsernameContainingIgnoreCaseAndTimestampBetweenOrderByTimestampDesc(action, username, fromDate, toDate).stream().map(AuditLogDTO::from).toList();
        }
        if (action != null && fromDate != null) {
            return auditLogRepository.findByActionContainingIgnoreCaseAndTimestampBetweenOrderByTimestampDesc(action, fromDate, toDate).stream().map(AuditLogDTO::from).toList();
        }
        if (username != null && fromDate != null) {
            return auditLogRepository.findByUserUsernameContainingIgnoreCaseAndTimestampBetweenOrderByTimestampDesc(username, fromDate, toDate).stream().map(AuditLogDTO::from).toList();
        }
        if (fromDate != null) {
            return auditLogRepository.findByTimestampBetweenOrderByTimestampDesc(fromDate, toDate).stream().map(AuditLogDTO::from).toList();
        }
        if (action != null) {
            return auditLogRepository.findByActionContainingIgnoreCaseOrderByTimestampDesc(action).stream().map(AuditLogDTO::from).toList();
        }
        if (username != null) {
            return auditLogRepository.findByUserUsernameContainingIgnoreCaseOrderByTimestampDesc(username).stream().map(AuditLogDTO::from).toList();
        }
        return auditLogRepository.findAllByOrderByTimestampDesc().stream().map(AuditLogDTO::from).toList();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        auditLogRepository.deleteById(id);
    }
}
