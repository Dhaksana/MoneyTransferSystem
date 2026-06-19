package com.bd.dto;

import java.util.List;

public record AnalyticsDTO(
        double moneySentThisMonth,
        double moneyReceivedThisMonth,
        long transactionCount,
        double largestTransaction,
        String mostFrequentBeneficiary,
        int rewardPointsEarnedMonthly,
        List<ChartPointDTO> monthlyTransactionTrend,
        List<ChartPointDTO> statusDistribution
) {
}
