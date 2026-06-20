package com.bd.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.bd.dto.RewardHistoryDTO;
import com.bd.dto.RewardSummaryDTO;
import com.bd.model.RewardRecord;
import com.bd.model.TransactionLog;
import com.bd.repository.RewardRecordRepository;

@Service
public class RewardService {

    private final RewardRecordRepository rewardRepo;

    public RewardService(RewardRecordRepository rewardRepo) {
        this.rewardRepo = rewardRepo;
    }

    public boolean isEligible(TransactionLog log) {
        if (log == null || log.getAmount() == null) {
            return false;
        }
        if (!"SUCCESS".equalsIgnoreCase(log.getStatus())) {
            return false;
        }
        if (log.getAmount() <= 100) {
            return false;
        }
        if (log.getFromAccountId() == null || log.getToAccountId() == null) {
            return false;
        }
        return !log.getFromAccountId().equals(log.getToAccountId());
    }

    public int calculateRewardPoints(double amount) {
        if (amount <= 100) {
            return 0;
        }
        return (int) Math.floor(amount / 100.0);
    }

    public RewardRecord grantReward(TransactionLog transactionLog) {
        if (transactionLog == null || transactionLog.getId() == null) {
            throw new IllegalArgumentException("Transaction log must be persisted before reward can be granted");
        }

        if (!isEligible(transactionLog)) {
            throw new IllegalStateException("Transaction is not eligible for rewards");
        }

        if (rewardRepo.findByTransactionLogId(transactionLog.getId()).isPresent()) {
            return rewardRepo.findByTransactionLogId(transactionLog.getId()).get();
        }

        RewardRecord reward = new RewardRecord();
        reward.setAccountId(transactionLog.getFromAccountId());
        reward.setTransactionLogId(transactionLog.getId());
        reward.setTransactionAmount(transactionLog.getAmount());
        reward.setPoints(calculateRewardPoints(transactionLog.getAmount()));
        reward.setRuleDescription("1 reward point per Rs 100 transferred for successful transfers above Rs 100");

        return rewardRepo.save(reward);
    }

    public List<RewardHistoryDTO> getRewardHistory(String accountId) {
        return rewardRepo.findByAccountIdOrderByCreatedOnDesc(accountId)
                .stream()
                .map(r -> new RewardHistoryDTO(
                        r.getId(),
                        r.getAccountId(),
                        r.getTransactionLogId(),
                        r.getPoints(),
                        r.getTransactionAmount(),
                        r.getRuleDescription(),
                        r.getCreatedOn()
                ))
                .collect(Collectors.toList());
    }

    public RewardSummaryDTO getRewardSummary(String accountId) {
        var records = rewardRepo.findByAccountIdOrderByCreatedOnDesc(accountId);
        int totalPoints = records.stream().mapToInt(r -> r.getPoints() == null ? 0 : r.getPoints()).sum();
        int rewardCount = records.size();
        return new RewardSummaryDTO(
                accountId,
                totalPoints,
                rewardCount,
                "1 reward point per Rs 100 transferred for successful transfers above Rs 100"
        );
    }
}
