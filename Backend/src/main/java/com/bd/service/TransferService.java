package com.bd.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bd.dto.TransactionHistoryDTO;
import com.bd.dto.TransferRequestDTO;
import com.bd.dto.TransferResponseDTO;
import com.bd.event.TransferCompletedEvent;
import com.bd.exception.AccountNotFoundException;
import com.bd.model.Account;
import com.bd.model.TransactionLog;
import com.bd.repository.AccountRepository;
import com.bd.repository.TransactionLogRepository;

@Service
public class TransferService implements ITransferService {

    private final AccountRepository accountRepo;
    private final TransactionLogRepository logRepo;
    private final FailureLogService failureLogService;
    private final ApplicationEventPublisher events;
    private final NotificationService notifications;

    public TransferService(AccountRepository accountRepo,
                           TransactionLogRepository logRepo,
                           FailureLogService failureLogService,
                           ApplicationEventPublisher events,
                           NotificationService notifications) {
        this.accountRepo = accountRepo;
        this.logRepo = logRepo;
        this.failureLogService = failureLogService;
        this.events = events;
        this.notifications = notifications;
    }

    @Override
    @Transactional
    public TransferResponseDTO transfer(TransferRequestDTO request) {
        TransactionLog log = new TransactionLog();
        log.setReferenceNumber(generateReferenceNumber());
        log.setAmount(request.getAmount());
        log.setIdempotencyKey(request.getIdempotencyKey());
        log.setTransactionType("TRANSFER");

        try {
            if (request.getIdempotencyKey() != null && !request.getIdempotencyKey().isBlank()) {
                logRepo.findByIdempotencyKey(request.getIdempotencyKey())
                        .ifPresent(t -> {
                            throw new IllegalStateException("Duplicate transfer request");
                        });
            }

            Account from = resolveAccount(request.getFromAccountNumber(), request.getFromAccountId());
            Account to = resolveAccount(request.getToAccountNumber(), request.getToAccountId());

            if (from.getId().equals(to.getId()) || from.getAccountNumber().equals(to.getAccountNumber())) {
                throw new IllegalArgumentException("Cannot transfer to same account");
            }

            log.setSenderAccount(from);
            log.setReceiverAccount(to);

            from.debit(request.getAmount());
            to.credit(request.getAmount());

            accountRepo.save(from);
            accountRepo.save(to);

            log.setStatus("SUCCESS");
            TransactionLog savedLog = logRepo.save(log);
            events.publishEvent(new TransferCompletedEvent(savedLog));

            return new TransferResponseDTO(savedLog.getId(), "SUCCESS", "Transfer completed successfully");
        } catch (Exception e) {
            log.setStatus("FAILED");
            log.setFailureReason(e.getMessage());
            try {
                failureLogService.saveFailureLog(log);
            } catch (Exception ignored) {
                // Keep failed transfer responses controlled even if failure logging cannot persist.
            }
            notifyTransferFailure(log, request, e.getMessage());
            return new TransferResponseDTO(log.getId(), "FAILED", e.getMessage());
        }
    }

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
                        t.getCreatedOn()
                ))
                .collect(Collectors.toList());
    }

    private String generateReferenceNumber() {
        return "TXN-" + UUID.randomUUID();
    }

    private Account resolveAccount(String accountNumber, String accountId) {
        if (accountNumber != null && !accountNumber.isBlank()) {
            return accountRepo.findByAccountNumber(accountNumber)
                    .orElseThrow(() -> new AccountNotFoundException(accountNumber));
        }
        return accountRepo.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    private void notifyTransferFailure(TransactionLog log, TransferRequestDTO request, String reason) {
        Account sender = log.getSenderAccount();
        if (sender == null && request.getFromAccountId() != null && !request.getFromAccountId().isBlank()) {
            sender = accountRepo.findById(request.getFromAccountId()).orElse(null);
        }
        if (sender == null && request.getFromAccountNumber() != null && !request.getFromAccountNumber().isBlank()) {
            sender = accountRepo.findByAccountNumber(request.getFromAccountNumber()).orElse(null);
        }
        if (sender != null && sender.getUser() != null) {
            notifications.create(sender.getUser(), "TRANSFER_FAILED", "Transfer failed: " + reason);
        }
    }
}
