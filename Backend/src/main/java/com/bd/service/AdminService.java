package com.bd.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bd.dto.AccountDTO;
import com.bd.dto.AdminTransactionDTO;
import com.bd.dto.AdminUserDTO;
import com.bd.dto.AuditLogDTO;
import com.bd.dto.RewardHistoryDTO;
import com.bd.dto.UserDetailDTO;
import com.bd.model.Account;
import com.bd.model.AppUser;
import com.bd.repository.AccountRepository;
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
    private final AccountRepository accounts;

    public AdminService(AppUserRepository users, TransactionLogRepository transactions, RewardTransactionRepository rewards, AuditLogRepository auditLogs, AuditService auditService, AccountRepository accounts) {
        this.users = users;
        this.transactions = transactions;
        this.rewards = rewards;
        this.auditLogs = auditLogs;
        this.auditService = auditService;
        this.accounts = accounts;
    }

    public List<AdminUserDTO> users() {
        return users.findAll().stream().map(AdminUserDTO::from).toList();
    }

    public UserDetailDTO getUserDetails(Long id) {
        AppUser user = users.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        List<AccountDTO> accountDTOs = accounts.findByUserId(id).stream().map(AccountDTO::toDTO).toList();
        return UserDetailDTO.from(user, accountDTOs);
    }

    @Transactional
    public AccountDTO updateAccount(String accountId, Map<String, Object> body) {
        Account account = accounts.findById(accountId).orElseThrow(() -> new IllegalArgumentException("Account not found"));
        if (body.containsKey("accountType")) account.setAccountType((String) body.get("accountType"));
        if (body.containsKey("holderName")) account.setHolderName((String) body.get("holderName"));
        if (body.containsKey("balance")) account.setBalance(((Number) body.get("balance")).doubleValue());
        if (body.containsKey("status")) account.setStatus((String) body.get("status"));
        auditService.log("ADMIN_ACCOUNT_UPDATED", "Account " + accountId + " updated");
        return AccountDTO.toDTO(account);
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
        if (body.containsKey("displayName")) user.setDisplayName(body.get("displayName"));
        if (body.containsKey("accountId")) user.setAccountId(body.get("accountId"));
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
