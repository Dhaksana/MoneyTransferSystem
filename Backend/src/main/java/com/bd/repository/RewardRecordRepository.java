package com.bd.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bd.model.RewardRecord;

public interface RewardRecordRepository extends JpaRepository<RewardRecord, Long> {
    List<RewardRecord> findByAccountIdOrderByCreatedOnDesc(String accountId);
    Optional<RewardRecord> findByTransactionLogId(Long transactionLogId);
}
