package com.bd.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.bd.model.Account;
import com.bd.model.AppUser;
import com.bd.model.RewardAccount;
import com.bd.model.RewardTransaction;
import com.bd.model.TransactionLog;
import com.bd.dto.RewardHistoryDTO;
import com.bd.dto.RewardSummaryDTO;
import com.bd.repository.RewardAccountRepository;
import com.bd.repository.RewardTransactionRepository;
import java.util.List;

@Service
public class RewardService {

    private final RewardAccountRepository rewardAccounts;
    private final RewardTransactionRepository rewardTransactions;
    private final NotificationService notifications;

    public RewardService(RewardAccountRepository rewardAccounts,
                         RewardTransactionRepository rewardTransactions,
                         NotificationService notifications) {
        this.rewardAccounts = rewardAccounts;
        this.rewardTransactions = rewardTransactions;
        this.notifications = notifications;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void awardForSuccessfulTransfer(TransactionLog transaction) {
        if (transaction == null || rewardTransactions.existsByBankingTransaction(transaction)) {
            return;
        }

        if (!"SUCCESS".equalsIgnoreCase(transaction.getStatus()) || transaction.getAmount() == null) {
            return;
        }

        int points = (int) Math.floor(transaction.getAmount() / 100);
        if (points <= 0) {
            return;
        }

        Account sender = transaction.getSenderAccount();
        Account receiver = transaction.getReceiverAccount();
        if (sender == null || receiver == null || sender.getUser() == null || receiver.getUser() == null) {
            return;
        }

        AppUser senderUser = sender.getUser();
        if (senderUser.getId().equals(receiver.getUser().getId())) {
            return;
        }

        RewardAccount rewardAccount = rewardAccounts.findByUser(senderUser)
                .orElseGet(() -> {
                    RewardAccount account = new RewardAccount();
                    account.setUser(senderUser);
                    return account;
                });

        rewardAccount.setCurrentPoints(rewardAccount.getCurrentPoints() + points);
        rewardAccount.setLifetimePoints(rewardAccount.getLifetimePoints() + points);
        RewardAccount savedAccount = rewardAccounts.save(rewardAccount);

        RewardTransaction rewardTransaction = new RewardTransaction();
        rewardTransaction.setRewardAccount(savedAccount);
        rewardTransaction.setBankingTransaction(transaction);
        rewardTransaction.setPointsEarned(points);
        rewardTransaction.setReason("TRANSFER_SUCCESS");
        rewardTransactions.save(rewardTransaction);
        notifications.create(senderUser, "REWARD_CREDITED", points + " reward points credited.");
    }

    public RewardSummaryDTO summary(String username) {
        RewardAccount account = rewardAccounts.findByUserUsername(username).orElse(null);
        if (account == null) {
            return new RewardSummaryDTO(0, 0);
        }
        return new RewardSummaryDTO(account.getCurrentPoints(), account.getLifetimePoints());
    }

    public List<RewardHistoryDTO> history(String username) {
        return rewardTransactions.findByRewardAccountUserUsernameOrderByCreatedAtDesc(username)
                .stream()
                .map(r -> new RewardHistoryDTO(
                        r.getId(),
                        r.getBankingTransaction().getId(),
                        r.getPointsEarned(),
                        r.getReason(),
                        r.getCreatedAt()
                ))
                .toList();
    }

    public long totalDistributed() {
        return rewardTransactions.totalPointsDistributed();
    }
}
