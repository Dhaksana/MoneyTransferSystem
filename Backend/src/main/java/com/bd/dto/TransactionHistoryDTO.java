package com.bd.dto;

import java.time.LocalDateTime;

public record TransactionHistoryDTO(
        Long transactionId,
        Integer fromAccountId,
        Integer toAccountId,
        Double amount,
        String status,
        String failureReason,
        LocalDateTime createdOn
) {}