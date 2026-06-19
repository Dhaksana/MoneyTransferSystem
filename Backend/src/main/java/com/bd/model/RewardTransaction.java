package com.bd.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "reward_transactions", indexes = {
        @Index(name = "idx_reward_transactions_account", columnList = "reward_account_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uq_reward_transaction_banking", columnNames = {"banking_transaction_id"})
})
public class RewardTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reward_account_id", nullable = false)
    private RewardAccount rewardAccount;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "banking_transaction_id", nullable = false, unique = true)
    private TransactionLog bankingTransaction;

    @Min(0)
    @Column(name = "points_earned", nullable = false)
    private int pointsEarned;

    @NotBlank
    @Column(nullable = false, length = 255)
    private String reason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public RewardTransaction() {}

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public RewardAccount getRewardAccount() { return rewardAccount; }
    public void setRewardAccount(RewardAccount rewardAccount) { this.rewardAccount = rewardAccount; }
    public TransactionLog getBankingTransaction() { return bankingTransaction; }
    public void setBankingTransaction(TransactionLog bankingTransaction) { this.bankingTransaction = bankingTransaction; }
    public int getPointsEarned() { return pointsEarned; }
    public void setPointsEarned(int pointsEarned) { this.pointsEarned = pointsEarned; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
