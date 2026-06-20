package com.bd.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.bd.dto.TransactionHistoryDTO;
import com.bd.dto.TransferRequestDTO;
import com.bd.dto.TransferResponseDTO;
import com.bd.dto.PaginatedResponse;
import com.bd.model.Account;
import com.bd.model.TransactionLog;
import com.bd.service.RewardService;
import com.bd.repository.AccountRepository;
import com.bd.repository.TransactionLogRepository;

@Service
public class TransferService implements ITransferService {

    private final AccountRepository accountRepo;
    private final TransactionLogRepository logRepo;
    private final FailureLogService failureLogService;
    private final RewardService rewardService;

    @Autowired
    public TransferService(AccountRepository accountRepo,
                           TransactionLogRepository logRepo,
                           FailureLogService failureLogService,
                           RewardService rewardService) {
        this.accountRepo = accountRepo;
        this.logRepo = logRepo;
        this.failureLogService = failureLogService;
        this.rewardService = rewardService;
    }

    // Backwards-compatible constructor for tests that don't provide RewardService
    public TransferService(AccountRepository accountRepo,
                           TransactionLogRepository logRepo,
                           FailureLogService failureLogService) {
        this.accountRepo = accountRepo;
        this.logRepo = logRepo;
        this.failureLogService = failureLogService;
        this.rewardService = null;
    }

    @Override
    @Transactional
    public TransferResponseDTO transfer(TransferRequestDTO request) {
        // First, ensure both accounts exist before doing any work
        if (!accountRepo.existsById(request.getFromAccountId())) {
            return new TransferResponseDTO(null, "FAILED", "From account not found", 0);
        }
        if (!accountRepo.existsById(request.getToAccountId())) {
            return new TransferResponseDTO(null, "FAILED", "To account not found", 0);
        }

        // Create log with request values (not yet persisted)
        TransactionLog log = new TransactionLog();
        log.setFromAccountId(request.getFromAccountId());
        log.setToAccountId(request.getToAccountId());
        log.setAmount(request.getAmount());
        log.setIdempotencyKey(request.getIdempotencyKey());

        try {

            // Idempotency check
            logRepo.findByIdempotencyKey(request.getIdempotencyKey())
                    .ifPresent(t -> {
                        throw new IllegalStateException("Duplicate transfer request");
                    });

            // Fetch accounts
            Account from = accountRepo.findById(request.getFromAccountId())
                    .orElseThrow(() -> new RuntimeException("From account not found"));

            Account to = accountRepo.findById(request.getToAccountId())
                    .orElseThrow(() -> new RuntimeException("To account not found"));

            if (from.getId().equals(to.getId())) {
                throw new IllegalArgumentException("Cannot transfer to same account");
            }

            // Perform transfer
            from.debit(request.getAmount());
            to.credit(request.getAmount());

            accountRepo.save(from);
            accountRepo.save(to);

            // Save success log and reward details
            log.setStatus("SUCCESS");
            int rewardPoints = (rewardService != null) ? rewardService.calculateRewardPoints(log.getAmount()) : 0;
            boolean eligible = (rewardService != null) ? rewardService.isEligible(log) : false;
            if (eligible) {
                log.setRewardPoints(rewardPoints);
            } else {
                log.setRewardPoints(0);
            }

            logRepo.save(log);
            if (rewardService != null && log.getRewardPoints() != null && log.getRewardPoints() > 0) {
                rewardService.grantReward(log);
            }

            return new TransferResponseDTO(
                    log.getId(),
                    "SUCCESS",
                    "Transfer completed successfully",
                    log.getRewardPoints()
            );

        } catch (Exception e) {

            // Save failure log in separate transaction (don't let save errors override response)
            log.setStatus("FAILED");
            log.setRewardPoints(0);
            log.setFailureReason(e.getMessage());
            try {
                failureLogService.saveFailureLog(log);
            } catch (Exception ex) {
                // ignore logging errors to ensure we return a controlled FAILED response
            }

            // Return FAILED response instead of throwing 500
            return new TransferResponseDTO(
                    log.getId(),
                    "FAILED",
                    e.getMessage(),
                    0
            );
        }
    }

    // ---------------- TRANSACTION HISTORY ----------------

    @Override
    public List<TransactionHistoryDTO> getTransactionHistory(String accountId) {
        return logRepo.findTransactionHistory(accountId)
                .stream()
                .map(t -> new TransactionHistoryDTO(
                        t.getId(),
                        t.getFromAccountId(),
                        t.getToAccountId(),
                        t.getAmount(),
                        t.getStatus(),
                        t.getFailureReason(),
                        t.getRewardPoints(),
                        t.getCreatedOn()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public PaginatedResponse<TransactionHistoryDTO> getTransactionHistoryPaginated(
            String accountId, int page, int size) {
        // Fetch all transactions for the account
        List<TransactionLog> allTransactions = logRepo.findTransactionHistory(accountId);
        
        // Calculate pagination
        int totalElements = allTransactions.size();
        int start = page * size;
        int end = Math.min(start + size, totalElements);
        
        // Get page content
        List<TransactionHistoryDTO> pageContent = allTransactions
                .subList(start, end)
                .stream()
                .map(t -> new TransactionHistoryDTO(
                        t.getId(),
                        t.getFromAccountId(),
                        t.getToAccountId(),
                        t.getAmount(),
                        t.getStatus(),
                        t.getFailureReason(),
                        t.getRewardPoints(),
                        t.getCreatedOn()
                ))
                .collect(Collectors.toList());
        
        return new PaginatedResponse<>(pageContent, page, size, totalElements);
    }

    @Override
    public PaginatedResponse<TransactionHistoryDTO> getTransactionHistoryPaginatedWithFilter(
            String accountId, int page, int size, String filter) {
        // Fetch all transactions for the account
        List<TransactionLog> allTransactions = logRepo.findTransactionHistory(accountId);
        
        // Apply filter
        if (filter != null && !filter.isEmpty() && !"all".equalsIgnoreCase(filter)) {
            allTransactions = allTransactions.stream()
                    .filter(t -> applyTransactionFilter(t, accountId, filter))
                    .collect(Collectors.toList());
        }
        
        // Calculate pagination
        int totalElements = allTransactions.size();
        int start = page * size;
        int end = Math.min(start + size, totalElements);
        
        // Get page content
        List<TransactionHistoryDTO> pageContent = allTransactions
                .subList(start, end)
                .stream()
                .map(t -> new TransactionHistoryDTO(
                        t.getId(),
                        t.getFromAccountId(),
                        t.getToAccountId(),
                        t.getAmount(),
                        t.getStatus(),
                        t.getFailureReason(),
                        t.getRewardPoints(),
                        t.getCreatedOn()
                ))
                .collect(Collectors.toList());
        
        return new PaginatedResponse<>(pageContent, page, size, totalElements);
    }

    // Helper method to apply filter logic
    private boolean applyTransactionFilter(TransactionLog transaction, String accountId, String filter) {
        switch (filter.toLowerCase()) {
            case "sent":
                return transaction.getFromAccountId().equals(accountId);
            case "received":
                return transaction.getToAccountId().equals(accountId);
            case "success":
                return "SUCCESS".equalsIgnoreCase(transaction.getStatus());
            case "failure":
                return "FAILED".equalsIgnoreCase(transaction.getStatus());
            default:
                return true;
        }
    }

    @Override
    public PaginatedResponse<TransactionHistoryDTO> getAllTransactionsPaginated(int page, int size) {
        return getAllTransactionsPaginated(page, size, null);
    }

    @Override
    public PaginatedResponse<TransactionHistoryDTO> getAllTransactionsPaginated(int page, int size, String transactionId) {
        // Fetch all transactions in the system (admin only)
        List<TransactionLog> allTransactions = logRepo.findAll();

        if (transactionId != null && !transactionId.isBlank()) {
            allTransactions = allTransactions.stream()
                    .filter(t -> t.getId() != null && t.getId().toString().contains(transactionId.trim()))
                    .collect(Collectors.toList());
        }
        
        // Calculate pagination
        int totalElements = allTransactions.size();
        int start = Math.max(0, page * size);
        int end = Math.min(start + size, totalElements);
        
        // Get page content
        List<TransactionHistoryDTO> pageContent = allTransactions
                .subList(start, end)
                .stream()
                .map(t -> new TransactionHistoryDTO(
                        t.getId(),
                        t.getFromAccountId(),
                        t.getToAccountId(),
                        t.getAmount(),
                        t.getStatus(),
                        t.getFailureReason(),
                        t.getRewardPoints(),
                        t.getCreatedOn()
                ))
                .collect(Collectors.toList());
        
        return new PaginatedResponse<>(pageContent, page, size, totalElements);
    }
}
