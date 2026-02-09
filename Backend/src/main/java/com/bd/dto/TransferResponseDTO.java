package com.bd.dto;

public class TransferResponseDTO {

    private Long transactionId;
    private String status;
    private String message;

    public TransferResponseDTO(Long transactionId, String status, String message) {
        this.transactionId = transactionId;
        this.status = status;
        this.message = message;
    }

    public Long getTransactionId() { return transactionId; }
    public String getStatus() { return status; }
    public String getMessage() { return message; }
}
