package com.bd.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

public class TransferRequestDTO {

    private String fromAccountId;

    private String toAccountId;

    private String fromAccountNumber;

    private String toAccountNumber;

    @NotNull(message = "Transfer amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
    private Double amount;

    private String idempotencyKey;

    @AssertTrue(message = "Source account id or account number is required")
    public boolean isSourcePresent() {
        return hasText(fromAccountId) || hasText(fromAccountNumber);
    }

    @AssertTrue(message = "Destination account id or account number is required")
    public boolean isDestinationPresent() {
        return hasText(toAccountId) || hasText(toAccountNumber);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    // getters & setters
    public String getFromAccountId() { return fromAccountId; }
    public void setFromAccountId(String fromAccountId) { this.fromAccountId = fromAccountId; }

    public String getToAccountId() { return toAccountId; }
    public void setToAccountId(String toAccountId) { this.toAccountId = toAccountId; }

    public String getFromAccountNumber() { return fromAccountNumber; }
    public void setFromAccountNumber(String fromAccountNumber) { this.fromAccountNumber = fromAccountNumber; }

    public String getToAccountNumber() { return toAccountNumber; }
    public void setToAccountNumber(String toAccountNumber) { this.toAccountNumber = toAccountNumber; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
}
