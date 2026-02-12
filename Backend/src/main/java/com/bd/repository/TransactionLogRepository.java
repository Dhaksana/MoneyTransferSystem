package com.bd.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bd.model.TransactionLog;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionLogRepository extends JpaRepository<TransactionLog, Long> {

    Optional<TransactionLog> findByIdempotencyKey(String idempotencyKey);

    // ✅ CUSTOM JPQL QUERY (MANDATORY REQUIREMENT)
    @Query("""
        SELECT t FROM TransactionLog t
        WHERE t.fromAccountId = :accountId
           OR t.toAccountId = :accountId
        ORDER BY t.createdOn DESC
    """)
        List<TransactionLog> findTransactionHistory(
            @Param("accountId") String accountId
        );
}
