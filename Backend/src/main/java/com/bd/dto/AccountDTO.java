package com.bd.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import com.bd.model.Account;

public class AccountDTO {

    private String id;

    @NotBlank(message = "Account name must not be blank")
    @Pattern(
        regexp = "^[A-Za-z ]{3,30}$",
        message = "Account name must be 3–30 characters and contain only letters"
    )
    private String holderName;

    @Min(value = 0, message = "Balance cannot be negative")
    private double balance;

    @NotNull(message = "Status is required")
    private String status;

    private Integer version;

    public AccountDTO() {}

    /* ---------- Entity → DTO ---------- */
    public static AccountDTO toDTO(Account account) {
        AccountDTO dto = new AccountDTO();
        dto.id = account.getId();
        dto.holderName = account.getHolderName();
        dto.balance = account.getBalance();
        dto.status = account.getStatus();
        dto.version = account.getVersion();
        return dto;
    }

    /* ---------- DTO → Entity ---------- */
    public static Account fromDTO(AccountDTO dto) {
        Account account = new Account();
        account.setId(dto.id);
        account.setHolderName(dto.holderName);
        account.setBalance(dto.balance);
        account.setStatus(dto.status);
        return account;
    }

    // getters & setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getHolderName() { return holderName; }
    public void setHolderName(String holderName) { this.holderName = holderName; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getVersion() { return version; }
}
