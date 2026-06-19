package com.bd.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import com.bd.dto.TransferRequestDTO;
import com.bd.dto.TransferResponseDTO;
import com.bd.model.Account;
import com.bd.model.AppUser;
import com.bd.model.TransactionLog;
import com.bd.repository.AccountRepository;
import com.bd.repository.TransactionLogRepository;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private AccountRepository accountRepo;

    @Mock
    private TransactionLogRepository logRepo;

    @Mock
    private FailureLogService failureLogService;

    @Mock
    private ApplicationEventPublisher events;

    @Mock
    private NotificationService notifications;

    @InjectMocks
    private TransferService transferService;

    @Captor
    private ArgumentCaptor<Account> accountCaptor;

    private AppUser user;

    @BeforeEach
    void setUp() {
        user = new AppUser();
        user.setId(1L);
        user.setUsername("testuser");
    }

    private Account createAccount(String id, String number, double balance, AppUser user) {
        Account a = new Account();
        a.setId(id);
        a.setAccountNumber(number);
        a.setBalance(balance);
        a.setStatus("ACTIVE");
        a.setUser(user);
        return a;
    }

    private TransferRequestDTO createRequest(String fromId, String toId, Double amount) {
        TransferRequestDTO r = new TransferRequestDTO();
        r.setFromAccountId(fromId);
        r.setToAccountId(toId);
        r.setAmount(amount);
        return r;
    }

    @Test
    void testTransferSuccess() {
        Account from = createAccount("from1", "ACC001", 1000, user);
        Account to = createAccount("to1", "ACC002", 500, user);

        TransferRequestDTO request = createRequest("from1", "to1", 500.0);

        when(accountRepo.findById("from1")).thenReturn(Optional.of(from));
        when(accountRepo.findById("to1")).thenReturn(Optional.of(to));
        when(accountRepo.save(any(Account.class))).thenAnswer(i -> i.getArgument(0));
        when(logRepo.save(any(TransactionLog.class))).thenAnswer(i -> {
            TransactionLog log = i.getArgument(0);
            ReflectionTestUtils.setField(log, "id", 1L);
            return log;
        });

        TransferResponseDTO response = transferService.transfer(request);

        assertEquals("SUCCESS", response.getStatus());
        assertEquals(1L, response.getTransactionId());
        assertEquals(500.0, from.getBalance(), 0.001);
        assertEquals(1000.0, to.getBalance(), 0.001);

        verify(accountRepo, times(2)).save(accountCaptor.capture());
        assertEquals(500.0, accountCaptor.getAllValues().get(0).getBalance(), 0.001);
        assertEquals(1000.0, accountCaptor.getAllValues().get(1).getBalance(), 0.001);
    }

    @Test
    void testTransferInsufficientBalance() {
        Account from = createAccount("from1", "ACC001", 50, user);
        Account to = createAccount("to1", "ACC002", 500, user);

        TransferRequestDTO request = createRequest("from1", "to1", 100.0);

        when(accountRepo.findById("from1")).thenReturn(Optional.of(from));
        when(accountRepo.findById("to1")).thenReturn(Optional.of(to));

        TransferResponseDTO response = transferService.transfer(request);

        assertEquals("FAILED", response.getStatus());
        assertTrue(response.getMessage().contains("Insufficient balance"));
    }

    @Test
    void testTransferToSameAccount() {
        Account from = createAccount("same1", "ACC001", 1000, user);

        TransferRequestDTO request = createRequest("same1", "same1", 100.0);

        when(accountRepo.findById("same1")).thenReturn(Optional.of(from));

        TransferResponseDTO response = transferService.transfer(request);

        assertEquals("FAILED", response.getStatus());
        assertTrue(response.getMessage().contains("Cannot transfer to same account"));
    }

    @Test
    void testTransferInvalidFromAccount() {
        TransferRequestDTO request = createRequest("nonexistent", "to1", 100.0);

        when(accountRepo.findById("nonexistent")).thenReturn(Optional.empty());

        TransferResponseDTO response = transferService.transfer(request);

        assertEquals("FAILED", response.getStatus());
        assertTrue(response.getMessage().contains("Account not found"));
    }

    @Test
    void testTransferInvalidToAccount() {
        Account from = createAccount("from1", "ACC001", 1000, user);

        TransferRequestDTO request = createRequest("from1", "nonexistent", 100.0);

        when(accountRepo.findById("from1")).thenReturn(Optional.of(from));
        when(accountRepo.findById("nonexistent")).thenReturn(Optional.empty());

        TransferResponseDTO response = transferService.transfer(request);

        assertEquals("FAILED", response.getStatus());
        assertTrue(response.getMessage().contains("Account not found"));
    }

    @Test
    void testTransferIdempotencyKeyDuplicate() {
        TransferRequestDTO request = new TransferRequestDTO();
        request.setAmount(100.0);
        request.setIdempotencyKey("dup-key");

        when(logRepo.findByIdempotencyKey("dup-key")).thenReturn(Optional.of(new TransactionLog()));

        TransferResponseDTO response = transferService.transfer(request);

        assertEquals("FAILED", response.getStatus());
        assertTrue(response.getMessage().contains("Duplicate transfer request"));
    }

    @Test
    void testTransferByAccountNumber() {
        Account from = createAccount("from1", "ACC001", 1000, user);
        Account to = createAccount("to1", "ACC002", 500, user);

        TransferRequestDTO request = new TransferRequestDTO();
        request.setFromAccountNumber("ACC001");
        request.setToAccountNumber("ACC002");
        request.setAmount(300.0);

        when(accountRepo.findByAccountNumber("ACC001")).thenReturn(Optional.of(from));
        when(accountRepo.findByAccountNumber("ACC002")).thenReturn(Optional.of(to));
        when(accountRepo.save(any(Account.class))).thenAnswer(i -> i.getArgument(0));
        when(logRepo.save(any(TransactionLog.class))).thenAnswer(i -> {
            TransactionLog log = i.getArgument(0);
            ReflectionTestUtils.setField(log, "id", 2L);
            return log;
        });

        TransferResponseDTO response = transferService.transfer(request);

        assertEquals("SUCCESS", response.getStatus());
        assertEquals(2L, response.getTransactionId());
        assertEquals(700.0, from.getBalance(), 0.001);
        assertEquals(800.0, to.getBalance(), 0.001);
    }

    @Test
    void testTransferZeroAmount() {
        Account from = createAccount("from1", "ACC001", 1000, user);
        Account to = createAccount("to1", "ACC002", 500, user);

        TransferRequestDTO request = createRequest("from1", "to1", 0.0);

        when(accountRepo.findById("from1")).thenReturn(Optional.of(from));
        when(accountRepo.findById("to1")).thenReturn(Optional.of(to));

        TransferResponseDTO response = transferService.transfer(request);

        assertEquals("FAILED", response.getStatus());
        assertTrue(response.getMessage().contains("Debit amount must be positive"));
    }

    @Test
    void testTransferNegativeAmount() {
        Account from = createAccount("from1", "ACC001", 1000, user);
        Account to = createAccount("to1", "ACC002", 500, user);

        TransferRequestDTO request = createRequest("from1", "to1", -100.0);

        when(accountRepo.findById("from1")).thenReturn(Optional.of(from));
        when(accountRepo.findById("to1")).thenReturn(Optional.of(to));

        TransferResponseDTO response = transferService.transfer(request);

        assertEquals("FAILED", response.getStatus());
        assertTrue(response.getMessage().contains("Debit amount must be positive"));
    }
}
