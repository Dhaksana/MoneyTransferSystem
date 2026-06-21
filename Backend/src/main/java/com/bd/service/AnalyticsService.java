package com.bd.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.bd.dto.AdminAnalyticsDTO;
import com.bd.dto.AnalyticsDTO;
import com.bd.dto.ChartPointDTO;
import com.bd.repository.AppUserRepository;
import com.bd.repository.RewardTransactionRepository;
import com.bd.repository.TransactionLogRepository;

@Service
public class AnalyticsService {
    private final TransactionLogRepository transactions;
    private final RewardTransactionRepository rewards;
    private final AppUserRepository users;

    public AnalyticsService(TransactionLogRepository transactions, RewardTransactionRepository rewards, AppUserRepository users) {
        this.transactions = transactions;
        this.rewards = rewards;
        this.users = users;
    }

    public AnalyticsDTO userAnalytics(String username) {
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        double sent = transactions.sumSentSince(username, monthStart);
        double received = transactions.sumReceivedSince(username, monthStart);
        long count = transactions.countUserTransactionsSince(username, monthStart);
        double largest = transactions.largestUserTransaction(username);
        int monthlyRewards = rewards.findByRewardAccountUserUsernameOrderByCreatedAtDesc(username)
                .stream()
                .filter(r -> !r.getCreatedAt().isBefore(monthStart))
                .mapToInt(r -> r.getPointsEarned())
                .sum();
        return new AnalyticsDTO(
                sent,
                received,
                count,
                largest,
                "-",
                monthlyRewards,
                List.of(new ChartPointDTO("Sent", sent), new ChartPointDTO("Received", received)),
                List.of(
                        new ChartPointDTO("Success", transactions.countByStatusAndSender(username, "SUCCESS")),
                        new ChartPointDTO("Failed", transactions.countByStatusAndSender(username, "FAILED"))
                )
        );
    }

    public AdminAnalyticsDTO adminAnalytics() {
        return new AdminAnalyticsDTO(
                users.count(),
                users.countByStatus("ACTIVE"),
                transactions.totalSuccessfulVolume(),
                transactions.countByStatus("SUCCESS"),
                transactions.countByStatus("FAILED"),
                rewards.totalPointsDistributed()
        );
    }
}
