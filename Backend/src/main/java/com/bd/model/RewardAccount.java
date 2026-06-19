package com.bd.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;

@Entity
@Table(name = "reward_accounts", indexes = {
        @Index(name = "idx_reward_accounts_user", columnList = "user_id")
})
public class RewardAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private AppUser user;

    @Min(0)
    @Column(name = "current_points", nullable = false)
    private int currentPoints = 0;

    @Min(0)
    @Column(name = "lifetime_points", nullable = false)
    private int lifetimePoints = 0;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public RewardAccount() {}

    @PrePersist
    public void prePersist() {
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }
    public int getCurrentPoints() { return currentPoints; }
    public void setCurrentPoints(int currentPoints) { this.currentPoints = currentPoints; }
    public int getLifetimePoints() { return lifetimePoints; }
    public void setLifetimePoints(int lifetimePoints) { this.lifetimePoints = lifetimePoints; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
