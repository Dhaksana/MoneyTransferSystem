package com.bd.service;

import com.bd.dto.TransactionHistoryDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bd.dto.TransferRequestDTO;
import com.bd.dto.TransferResponseDTO;
import com.bd.model.Account;
import com.bd.model.TransactionLog;
import com.bd.repository.AccountRepository;
import com.bd.repository.TransactionLogRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransferService implements ITransferService {

    private final AccountRepository accountRepo;
    private final TransactionLogRepository logRepo;
    private final FailureLogService failureLogService;

    public TransferService(AccountRepository accountRepo,
                           TransactionLogRepository logRepo,
                           FailureLogService failureLogService) {
        this.accountRepo = accountRepo;
        this.logRepo = logRepo;
        this.failureLogService = failureLogService;
    }

    @Override
    @Transactional
    public TransferResponseDTO transfer(TransferRequestDTO request) {

        // ✅ Create log immediately with request values
        TransactionLog log = new TransactionLog();
        log.setFromAccountId(request.getFromAccountId());
        log.setToAccountId(request.getToAccountId());
        log.setAmount(request.getAmount());
        log.setIdempotencyKey(request.getIdempotencyKey());

        try {

            // 🔹 Idempotency check
            logRepo.findByIdempotencyKey(request.getIdempotencyKey())
                    .ifPresent(t -> {
                        throw new IllegalStateException("Duplicate transfer request");
                    });

            // 🔹 Fetch accounts
            Account from = accountRepo.findById(request.getFromAccountId())
                    .orElseThrow(() -> new RuntimeException("From account not found"));

            Account to = accountRepo.findById(request.getToAccountId())
                    .orElseThrow(() -> new RuntimeException("To account not found"));

            if (from.getId().equals(to.getId())) {
                throw new IllegalArgumentException("Cannot transfer to same account");
            }

            // 🔹 Perform transfer
            from.debit(request.getAmount());
            to.credit(request.getAmount());

            accountRepo.save(from);
            accountRepo.save(to);

            // 🔹 Save success log
            log.setStatus("SUCCESS");
            logRepo.save(log);

            return new TransferResponseDTO(
                    log.getId(),
                    "SUCCESS",
                    "Transfer completed successfully"
            );

        } catch (Exception e) {

            // 🔹 Save failure log in separate transaction
            log.setStatus("FAILED");
            log.setFailureReason(e.getMessage());
            failureLogService.saveFailureLog(log);

            // 🔹 Return FAILED response instead of throwing 500
            return new TransferResponseDTO(
                    log.getId(),
                    "FAILED",
                    e.getMessage()
            );
        }
    }

    // ---------------- TRANSACTION HISTORY ----------------

    @Override
    public List<TransactionHistoryDTO> getTransactionHistory(Integer accountId) {

        return logRepo.findTransactionHistory(accountId)
                .stream()
                .map(t -> new TransactionHistoryDTO(
                        t.getId(),
                        t.getFromAccountId(),
                        t.getToAccountId(),
                        t.getAmount(),
                        t.getStatus(),
                        t.getFailureReason(),
                        t.getCreatedOn()
                ))
                .collect(Collectors.toList());
    }
}
