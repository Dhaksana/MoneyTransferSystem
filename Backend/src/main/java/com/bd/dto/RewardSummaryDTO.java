package com.bd.dto;

public record RewardSummaryDTO(
        String accountId,
        Integer totalPoints,
        Integer rewardCount,
        String ruleDescription
) {}
