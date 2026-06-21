package com.bd.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bd.model.TransactionLog;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;

public interface TransactionLogRepository extends JpaRepository<TransactionLog, Long> {

    Optional<TransactionLog> findByIdempotencyKey(String idempotencyKey);

    // ✅ CUSTOM JPQL QUERY (MANDATORY REQUIREMENT)
    @Query("""
        SELECT t FROM TransactionLog t
        WHERE t.senderAccount.id = :accountId
           OR t.receiverAccount.id = :accountId
        ORDER BY t.createdAt DESC
    """)
    List<TransactionLog> findTransactionHistory(
            @Param("accountId") String accountId
    );

    @Query("select coalesce(sum(t.amount), 0) from TransactionLog t where t.senderAccount.user.username = :username and t.status = 'SUCCESS' and t.createdAt >= :start")
    double sumSentSince(@Param("username") String username, @Param("start") LocalDateTime start);

    @Query("select coalesce(sum(t.amount), 0) from TransactionLog t where t.receiverAccount.user.username = :username and t.status = 'SUCCESS' and t.createdAt >= :start")
    double sumReceivedSince(@Param("username") String username, @Param("start") LocalDateTime start);

    @Query("select count(t) from TransactionLog t where (t.senderAccount.user.username = :username or t.receiverAccount.user.username = :username) and t.createdAt >= :start")
    long countUserTransactionsSince(@Param("username") String username, @Param("start") LocalDateTime start);

    @Query("select coalesce(max(t.amount), 0) from TransactionLog t where (t.senderAccount.user.username = :username or t.receiverAccount.user.username = :username) and t.status = 'SUCCESS'")
    double largestUserTransaction(@Param("username") String username);

    @Query("select coalesce(sum(t.amount), 0) from TransactionLog t where t.status = 'SUCCESS'")
    double totalSuccessfulVolume();

    long countByStatus(String status);

    @Query("select count(t) from TransactionLog t where t.senderAccount.user.username = :username and t.status = :status")
    long countByStatusAndSender(@Param("username") String username, @Param("status") String status);

    @Query("""
        select t from TransactionLog t
        where (t.senderAccount.id = :accountId or t.receiverAccount.id = :accountId)
          and t.createdAt between :start and :end
        order by t.createdAt asc
    """)
    List<TransactionLog> findAccountStatementTransactions(
            @Param("accountId") String accountId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
