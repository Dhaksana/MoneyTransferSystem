package com.bd.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "reward_records")
public class RewardRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String accountId;

    @Column(nullable = false, unique = true)
    private Long transactionLogId;

    @Column(nullable = false)
    private Integer points;

    @Column(nullable = false)
    private Double transactionAmount;

    @Column(nullable = false)
    private String ruleDescription;

    @Column(nullable = false)
    private LocalDateTime createdOn;

    @PrePersist
    public void onCreate() {
        this.createdOn = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public Long getTransactionLogId() {
        return transactionLogId;
    }

    public void setTransactionLogId(Long transactionLogId) {
        this.transactionLogId = transactionLogId;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public Double getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(Double transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public String getRuleDescription() {
        return ruleDescription;
    }

    public void setRuleDescription(String ruleDescription) {
        this.ruleDescription = ruleDescription;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }
}
