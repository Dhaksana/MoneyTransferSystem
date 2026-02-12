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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

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

    @Version
    private Integer version;

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    public Account() {}

    /* ✅ AUTOMATIC timestamp handling */
    @PrePersist
    public void onCreate() {
        this.lastUpdated = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.lastUpdated = LocalDateTime.now();
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
    this.lastUpdated = LocalDateTime.now();
	}

	public void credit(double amount) {

		if (!"ACTIVE".equals(this.status)) {
            throw new InactiveAccountException();
		}

		if (amount <= 0) {
			throw new IllegalArgumentException("Credit amount must be positive");
		}

		this.balance += amount;
		this.lastUpdated = LocalDateTime.now();
	}


    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(this.status);
    }

    // ---------- getters & setters ----------
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getHolderName() { return holderName; }
    public void setHolderName(String holderName) { this.holderName = holderName; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getVersion() { return version; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
}
