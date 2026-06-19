package com.bd.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bd.model.Account;
import com.bd.model.AppUser;
import com.bd.model.RewardAccount;
import com.bd.model.RewardTransaction;
import com.bd.model.TransactionLog;
import com.bd.repository.RewardAccountRepository;
import com.bd.repository.RewardTransactionRepository;

@ExtendWith(MockitoExtension.class)
public class RewardServiceTest {

    @Mock
    private RewardAccountRepository rewardAccounts;

    @Mock
    private RewardTransactionRepository rewardTransactions;

    @Mock
    private NotificationService notifications;

    @InjectMocks
    private RewardService rewardService;

    @Captor
    private ArgumentCaptor<RewardAccount> rewardAccountCaptor;

    @Captor
    private ArgumentCaptor<RewardTransaction> rewardTransactionCaptor;

    private AppUser createUser(Long id, String username) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setUsername(username);
        return user;
    }

    private Account createAccount(AppUser user) {
        Account account = new Account();
        account.setUser(user);
        return account;
    }

    private TransactionLog createTransaction(String status, double amount, AppUser sender, AppUser receiver) {
        TransactionLog transaction = new TransactionLog();
        transaction.setStatus(status);
        transaction.setAmount(amount);
        if (sender != null) {
            transaction.setSenderAccount(createAccount(sender));
        }
        if (receiver != null) {
            transaction.setReceiverAccount(createAccount(receiver));
        }
        return transaction;
    }

    @Test
    public void testAward99Rupees() {
        AppUser sender = createUser(1L, "sender");
        AppUser receiver = createUser(2L, "receiver");
        TransactionLog transaction = createTransaction("SUCCESS", 99.0, sender, receiver);

        rewardService.awardForSuccessfulTransfer(transaction);

        verify(rewardAccounts, never()).save(any());
        verify(rewardTransactions, never()).save(any());
    }

    @Test
    public void testAward100Rupees() {
        AppUser sender = createUser(1L, "sender");
        AppUser receiver = createUser(2L, "receiver");
        TransactionLog transaction = createTransaction("SUCCESS", 100.0, sender, receiver);

        when(rewardAccounts.findByUser(sender)).thenReturn(Optional.empty());

        rewardService.awardForSuccessfulTransfer(transaction);

        verify(rewardAccounts).save(rewardAccountCaptor.capture());
        RewardAccount saved = rewardAccountCaptor.getValue();
        assertEquals(1, saved.getCurrentPoints());
        assertEquals(1, saved.getLifetimePoints());

        verify(rewardTransactions).save(rewardTransactionCaptor.capture());
        assertEquals(1, rewardTransactionCaptor.getValue().getPointsEarned());

        verify(notifications).create(eq(sender), eq("REWARD_CREDITED"), anyString());
    }

    @Test
    public void testAward550Rupees() {
        AppUser sender = createUser(1L, "sender");
        AppUser receiver = createUser(2L, "receiver");
        TransactionLog transaction = createTransaction("SUCCESS", 550.0, sender, receiver);

        when(rewardAccounts.findByUser(sender)).thenReturn(Optional.empty());

        rewardService.awardForSuccessfulTransfer(transaction);

        verify(rewardAccounts).save(rewardAccountCaptor.capture());
        RewardAccount saved = rewardAccountCaptor.getValue();
        assertEquals(5, saved.getCurrentPoints());
        assertEquals(5, saved.getLifetimePoints());

        verify(rewardTransactions).save(rewardTransactionCaptor.capture());
        assertEquals(5, rewardTransactionCaptor.getValue().getPointsEarned());

        verify(notifications).create(eq(sender), eq("REWARD_CREDITED"), anyString());
    }

    @Test
    public void testFailedTransactionGivesNoPoints() {
        AppUser sender = createUser(1L, "sender");
        AppUser receiver = createUser(2L, "receiver");
        TransactionLog transaction = createTransaction("FAILED", 200.0, sender, receiver);

        rewardService.awardForSuccessfulTransfer(transaction);

        verify(rewardAccounts, never()).save(any());
        verify(rewardTransactions, never()).save(any());
        verify(notifications, never()).create(any(), any(), any());
    }

    @Test
    public void testSameUserTransferGivesNoPoints() {
        AppUser user = createUser(1L, "user");
        TransactionLog transaction = createTransaction("SUCCESS", 200.0, user, user);

        rewardService.awardForSuccessfulTransfer(transaction);

        verify(rewardAccounts, never()).save(any());
        verify(rewardTransactions, never()).save(any());
        verify(notifications, never()).create(any(), any(), any());
    }

    @Test
    public void testDuplicateRewardPrevented() {
        AppUser sender = createUser(1L, "sender");
        AppUser receiver = createUser(2L, "receiver");
        TransactionLog transaction = createTransaction("SUCCESS", 200.0, sender, receiver);

        when(rewardTransactions.existsByBankingTransaction(transaction)).thenReturn(true);

        rewardService.awardForSuccessfulTransfer(transaction);

        verify(rewardAccounts, never()).save(any());
        verify(rewardTransactions, never()).save(any());
        verify(notifications, never()).create(any(), any(), any());
    }

    @Test
    public void testNullTransaction() {
        rewardService.awardForSuccessfulTransfer(null);

        verify(rewardAccounts, never()).save(any());
        verify(rewardTransactions, never()).save(any());
        verify(notifications, never()).create(any(), any(), any());
    }

    @Test
    public void testNullAmount() {
        AppUser sender = createUser(1L, "sender");
        AppUser receiver = createUser(2L, "receiver");
        TransactionLog transaction = new TransactionLog();
        transaction.setStatus("SUCCESS");
        transaction.setAmount(null);
        transaction.setSenderAccount(createAccount(sender));
        transaction.setReceiverAccount(createAccount(receiver));

        rewardService.awardForSuccessfulTransfer(transaction);

        verify(rewardAccounts, never()).save(any());
        verify(rewardTransactions, never()).save(any());
        verify(notifications, never()).create(any(), any(), any());
    }

    @Test
    public void testExistingRewardAccount() {
        AppUser sender = createUser(1L, "sender");
        AppUser receiver = createUser(2L, "receiver");
        TransactionLog transaction = createTransaction("SUCCESS", 300.0, sender, receiver);

        RewardAccount existing = new RewardAccount();
        existing.setUser(sender);
        existing.setCurrentPoints(10);
        existing.setLifetimePoints(50);

        when(rewardAccounts.findByUser(sender)).thenReturn(Optional.of(existing));

        rewardService.awardForSuccessfulTransfer(transaction);

        verify(rewardAccounts).save(rewardAccountCaptor.capture());
        RewardAccount saved = rewardAccountCaptor.getValue();
        assertEquals(13, saved.getCurrentPoints());
        assertEquals(53, saved.getLifetimePoints());

        verify(rewardTransactions).save(rewardTransactionCaptor.capture());
        assertEquals(3, rewardTransactionCaptor.getValue().getPointsEarned());

        verify(notifications).create(eq(sender), eq("REWARD_CREDITED"), anyString());
    }

    @Test
    public void testMultipleAwardsAccumulate() {
        AppUser sender = createUser(1L, "sender");
        AppUser receiver = createUser(2L, "receiver");
        TransactionLog transaction = createTransaction("SUCCESS", 200.0, sender, receiver);

        when(rewardAccounts.findByUser(sender)).thenReturn(Optional.empty());

        rewardService.awardForSuccessfulTransfer(transaction);

        verify(rewardAccounts).save(rewardAccountCaptor.capture());
        RewardAccount saved = rewardAccountCaptor.getValue();
        assertEquals(2, saved.getCurrentPoints());
        assertEquals(2, saved.getLifetimePoints());

        verify(rewardTransactions).save(rewardTransactionCaptor.capture());
        assertEquals(2, rewardTransactionCaptor.getValue().getPointsEarned());

        verify(notifications).create(eq(sender), eq("REWARD_CREDITED"), anyString());
    }

}
