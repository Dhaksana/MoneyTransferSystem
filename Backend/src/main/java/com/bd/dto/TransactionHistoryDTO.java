package com.bd.dto;

import java.time.LocalDateTime;

public record TransactionHistoryDTO(
        Long transactionId,
        String fromAccountId,
        String toAccountId,
        Double amount,
        String status,
        String failureReason,
        LocalDateTime createdOn
) {}