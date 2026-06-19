package com.bd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.bd.model.RewardTransaction;
import com.bd.model.TransactionLog;
import java.util.List;

public interface RewardTransactionRepository extends JpaRepository<RewardTransaction, Long> {
    boolean existsByBankingTransaction(TransactionLog bankingTransaction);
    List<RewardTransaction> findByRewardAccountUserUsernameOrderByCreatedAtDesc(String username);

    @Query("select coalesce(sum(r.pointsEarned), 0) from RewardTransaction r")
    long totalPointsDistributed();

    @Query("select coalesce(sum(r.pointsEarned), 0) from RewardTransaction r where r.bankingTransaction.id = :transactionId")
    int pointsForTransaction(Long transactionId);

    @Query("""
        select coalesce(sum(r.pointsEarned), 0) from RewardTransaction r
        where r.rewardAccount.user.username = :username
          and r.createdAt between :start and :end
    """)
    int pointsForUserBetween(String username, java.time.LocalDateTime start, java.time.LocalDateTime end);
}
