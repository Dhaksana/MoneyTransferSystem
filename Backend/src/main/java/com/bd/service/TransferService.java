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

    public TransferService(AccountRepository accountRepo,
                           TransactionLogRepository logRepo) {
        this.accountRepo = accountRepo;
        this.logRepo = logRepo;
    }

    @Override
    @Transactional
    public TransferResponseDTO transfer(TransferRequestDTO request) {

        // Idempotency check
        logRepo.findByIdempotencyKey(request.getIdempotencyKey())
                .ifPresent(t -> {
                    throw new IllegalStateException("Duplicate transfer request");
                });

        Account from = accountRepo.findById(request.getFromAccountId())
                .orElseThrow(() -> new RuntimeException("From account not found"));

        Account to = accountRepo.findById(request.getToAccountId())
                .orElseThrow(() -> new RuntimeException("To account not found"));

        if (from.getId().equals(to.getId())) {
            throw new IllegalArgumentException("Cannot transfer to same account");
        }

        TransactionLog log = new TransactionLog();
        log.setFromAccountId(from.getId());
        log.setToAccountId(to.getId());
        log.setAmount(request.getAmount());
        log.setIdempotencyKey(request.getIdempotencyKey());

        try {
            from.debit(request.getAmount());
            to.credit(request.getAmount());

            accountRepo.save(from);
            accountRepo.save(to);

            log.setStatus("SUCCESS");
            logRepo.save(log);

            return new TransferResponseDTO(
                    log.getId(),
                    "SUCCESS",
                    "Transfer completed successfully"
            );

        } catch (Exception e) {
            log.setStatus("FAILED");
            log.setFailureReason(e.getMessage());
            logRepo.save(log);
            throw e;
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
