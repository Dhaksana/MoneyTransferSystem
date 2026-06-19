package com.bd.dto;

public record AdminAnalyticsDTO(
        long totalUsers,
        long activeUsers,
        double totalTransactionVolume,
        long successfulTransactions,
        long failedTransactions,
        long totalRewardsDistributed
) {
}
