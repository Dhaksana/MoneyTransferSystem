package com.bd.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "holder_name", nullable = false)
    private String holderName;

    @Column(nullable = false)
    private double balance;

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
        throw new IllegalStateException("Account is not ACTIVE");
    }

    if (amount <= 0) {
        throw new IllegalArgumentException("Debit amount must be positive");
    }

    if (this.balance < amount) {
        throw new IllegalStateException("Insufficient balance");
    }

    this.balance -= amount;
    this.lastUpdated = LocalDateTime.now();
	}

	public void credit(double amount) {

		if (!"ACTIVE".equals(this.status)) {
			throw new IllegalStateException("Account is not ACTIVE");
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
