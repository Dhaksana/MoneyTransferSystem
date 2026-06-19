package com.bd.dto;

import java.time.LocalDateTime;

public record RewardHistoryDTO(Long id, Long transactionId, int pointsEarned, String reason, LocalDateTime createdAt) {
}
