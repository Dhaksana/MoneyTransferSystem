package com.bd.dto;

import java.time.LocalDateTime;

public record RewardHistoryDTO(
        Long rewardId,
        String accountId,
        Long transactionId,
        Integer points,
        Double transactionAmount,
        String ruleDescription,
        LocalDateTime createdOn
) {}
