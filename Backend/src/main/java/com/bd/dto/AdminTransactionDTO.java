package com.bd.dto;

import java.time.LocalDateTime;

import com.bd.model.TransactionLog;

public record AdminTransactionDTO(Long id, String referenceNumber, String fromAccount, String toAccount, Double amount, String status, LocalDateTime createdAt) {
    public static AdminTransactionDTO from(TransactionLog transaction) {
        return new AdminTransactionDTO(
                transaction.getId(),
                transaction.getReferenceNumber(),
                transaction.getFromAccountId(),
                transaction.getToAccountId(),
                transaction.getAmount(),
                transaction.getStatus(),
                transaction.getCreatedAt()
        );
    }
}
