package com.bd.dto;

import java.time.LocalDateTime;

public record TransactionHistoryDTO(
                Long transactionId,
                String fromAccountId,
                String toAccountId,
                Double amount,
                String status,
                String failureReason,
                Integer rewardPoints,
                LocalDateTime createdOn
) {
        // Backwards-compatible constructor for code/tests that expect the older 7-arg form
        // (transactionId, fromAccountId, toAccountId, amount, status, failureReason, createdOn)
        // Inserts a null rewardPoints value.
        public TransactionHistoryDTO(Long transactionId, String fromAccountId, String toAccountId,
                                                                 Double amount, String status, String failureReason, LocalDateTime createdOn) {
                this(transactionId, fromAccountId, toAccountId, amount, status, failureReason, null, createdOn);
        }
}