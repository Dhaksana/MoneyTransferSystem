package com.bd.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "beneficiaries", indexes = {
        @Index(name = "idx_beneficiaries_owner", columnList = "owner_account_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uq_beneficiary_owner_account", columnNames = {"owner_account_id", "beneficiary_account_number"})
})
public class Beneficiary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_account_id", nullable = false)
    private Account ownerAccount;

    @NotBlank
    @Size(max = 150)
    @Column(name = "beneficiary_name", nullable = false, length = 150)
    private String beneficiaryName;

    @NotBlank
    @Size(max = 64)
    @Column(name = "beneficiary_account_number", nullable = false, length = 64)
    private String beneficiaryAccountNumber;

    @Size(max = 150)
    @Column(name = "bank_name", length = 150)
    private String bankName;

    @Size(max = 20)
    @Column(name = "ifsc", length = 20)
    private String ifsc;

    @Size(max = 100)
    @Column(name = "nickname", length = 100)
    private String nickname;

    @Column(nullable = false)
    private boolean favorite = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Beneficiary() {}

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Account getOwnerAccount() { return ownerAccount; }
    public void setOwnerAccount(Account ownerAccount) { this.ownerAccount = ownerAccount; }
    public String getBeneficiaryName() { return beneficiaryName; }
    public void setBeneficiaryName(String beneficiaryName) { this.beneficiaryName = beneficiaryName; }
    public String getBeneficiaryAccountNumber() { return beneficiaryAccountNumber; }
    public void setBeneficiaryAccountNumber(String beneficiaryAccountNumber) { this.beneficiaryAccountNumber = beneficiaryAccountNumber; }
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    public String getIfsc() { return ifsc; }
    public void setIfsc(String ifsc) { this.ifsc = ifsc; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public boolean isFavorite() { return favorite; }
    public void setFavorite(boolean favorite) { this.favorite = favorite; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
