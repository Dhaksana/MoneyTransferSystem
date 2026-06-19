package com.bd.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bd.dto.AdminTransactionDTO;
import com.bd.dto.AdminUserDTO;
import com.bd.dto.AuditLogDTO;
import com.bd.dto.RewardHistoryDTO;
import com.bd.model.AppUser;
import com.bd.repository.AppUserRepository;
import com.bd.repository.AuditLogRepository;
import com.bd.repository.RewardTransactionRepository;
import com.bd.repository.TransactionLogRepository;

@Service
public class AdminService {
    private final AppUserRepository users;
    private final TransactionLogRepository transactions;
    private final RewardTransactionRepository rewards;
    private final AuditLogRepository auditLogs;
    private final AuditService auditService;

    public AdminService(AppUserRepository users, TransactionLogRepository transactions, RewardTransactionRepository rewards, AuditLogRepository auditLogs, AuditService auditService) {
        this.users = users;
        this.transactions = transactions;
        this.rewards = rewards;
        this.auditLogs = auditLogs;
        this.auditService = auditService;
    }

    public List<AdminUserDTO> users() {
        return users.findAll().stream().map(AdminUserDTO::from).toList();
    }

    @Transactional
    public AdminUserDTO setUserStatus(Long id, String status) {
        AppUser user = users.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setStatus(status);
        auditService.log("ADMIN_USER_STATUS_CHANGED", "User " + user.getUsername() + " status changed to " + status);
        return AdminUserDTO.from(user);
    }

    @Transactional
    public AdminUserDTO updateUser(Long id, Map<String, String> body) {
        AppUser user = users.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (body.containsKey("fullName")) user.setFullName(body.get("fullName"));
        if (body.containsKey("email")) user.setEmail(body.get("email"));
        if (body.containsKey("role")) user.setRole(body.get("role"));
        if (body.containsKey("status")) user.setStatus(body.get("status"));
        auditService.log("ADMIN_USER_UPDATED", "User " + user.getUsername() + " details updated");
        return AdminUserDTO.from(user);
    }

    public List<AdminTransactionDTO> transactions() {
        return transactions.findAll().stream().map(AdminTransactionDTO::from).toList();
    }

    public List<RewardHistoryDTO> rewards() {
        return rewards.findAll().stream()
                .map(r -> new RewardHistoryDTO(r.getId(), r.getBankingTransaction().getId(), r.getPointsEarned(), r.getReason(), r.getCreatedAt()))
                .toList();
    }

    public List<AuditLogDTO> auditLogs() {
        return auditLogs.findAllByOrderByTimestampDesc().stream().map(AuditLogDTO::from).toList();
    }
}
