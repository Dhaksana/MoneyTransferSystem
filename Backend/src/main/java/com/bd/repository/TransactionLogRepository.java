package com.bd.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bd.model.TransactionLog;

public interface TransactionLogRepository extends JpaRepository<TransactionLog, Long> {

    Optional<TransactionLog> findByIdempotencyKey(String idempotencyKey);
}
