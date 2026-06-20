package com.bd.dto;

public class TransferResponseDTO {

    private Long transactionId;
    private String status;
    private String message;
    private Integer rewardPoints;

    public TransferResponseDTO(Long transactionId, String status, String message, Integer rewardPoints) {
        this.transactionId = transactionId;
        this.status = status;
        this.message = message;
        this.rewardPoints = rewardPoints;
    }

    public Long getTransactionId() { return transactionId; }
    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public Integer getRewardPoints() { return rewardPoints; }
}
