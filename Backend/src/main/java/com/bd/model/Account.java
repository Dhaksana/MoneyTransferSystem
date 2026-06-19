package com.bd.model;

import java.time.LocalDateTime;

import com.bd.exception.InactiveAccountException;
import com.bd.exception.InsufficientBalanceException;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @Column(length = 64)
    private String id;

    @NotBlank(message = "Account number is required")
    @Size(max = 64)
    @Column(name = "account_number", nullable = false, unique = true, length = 64)
    private String accountNumber;

    @NotBlank(message = "Account type is required")
    @Column(name = "account_type", nullable = false, length = 50)
    private String accountType;

    @NotBlank(message = "Holder name is required")
    @Size(min = 3, max = 50, message = "Holder name must be 3–50 characters")
    @Column(name = "holder_name", nullable = false)
    private String holderName;

    @PositiveOrZero(message = "Balance cannot be negative")
    @Column(nullable = false)
    private double balance;

    @NotBlank
    @Pattern(
            regexp = "ACTIVE|INACTIVE|BLOCKED",
            message = "Status must be ACTIVE, INACTIVE, or BLOCKED"
    )
    @Column(nullable = false)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Version
    private Integer version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Account() {}

    /* ✅ AUTOMATIC timestamp handling */
    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ---------- business logic ----------
    public void debit(double amount) {
        if (!"ACTIVE".equals(this.status)) {
            throw new InactiveAccountException();
        }

        if (amount <= 0) {
            throw new IllegalArgumentException("Debit amount must be positive");
        }

        if (this.balance < amount) {
            throw new InsufficientBalanceException(this.balance, amount);
        }

        this.balance -= amount;
        this.updatedAt = LocalDateTime.now();
    }

    public void credit(double amount) {
        if (!"ACTIVE".equals(this.status)) {
            throw new InactiveAccountException();
        }

        if (amount <= 0) {
            throw new IllegalArgumentException("Credit amount must be positive");
        }

        this.balance += amount;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(this.status);
    }

    // ---------- getters & setters ----------
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public String getHolderName() { return holderName; }
    public void setHolderName(String holderName) { this.holderName = holderName; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }

    public Integer getVersion() { return version; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
