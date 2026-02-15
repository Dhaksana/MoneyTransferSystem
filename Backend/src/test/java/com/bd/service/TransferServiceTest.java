package com.bd.service;

import com.bd.dto.TransactionHistoryDTO;
import com.bd.dto.TransferRequestDTO;
import com.bd.dto.TransferResponseDTO;
import com.bd.model.Account;
import com.bd.model.TransactionLog;
import com.bd.repository.AccountRepository;
import com.bd.repository.TransactionLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("TransferService Tests")
class TransferServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionLogRepository transactionLogRepository;

    @Mock
    private FailureLogService failureLogService;

    private TransferService transferService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        transferService = new TransferService(accountRepository, transactionLogRepository, failureLogService);
    }

    // ============ SUCCESSFUL TRANSFER TESTS ============

    @Test
    @DisplayName("Should transfer funds successfully between accounts")
    void testTransferSuccess() {
        TransferRequestDTO request = new TransferRequestDTO();
        request.setFromAccountId("MTS2026-11111111");
        request.setToAccountId("MTS2026-22222222");
        request.setAmount(100.0);
        request.setIdempotencyKey("key-123");

        Account fromAccount = createAccount("MTS2026-11111111", "Sender", 500.0, "ACTIVE");
        Account toAccount = createAccount("MTS2026-22222222", "Receiver", 0.0, "ACTIVE");

        when(transactionLogRepository.findByIdempotencyKey("key-123")).thenReturn(Optional.empty());
        when(accountRepository.findById("MTS2026-11111111")).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById("MTS2026-22222222")).thenReturn(Optional.of(toAccount));
        when(transactionLogRepository.save(any(TransactionLog.class))).thenAnswer(invocation -> {
            TransactionLog log = invocation.getArgument(0);
            log.setId(1L);
            return log;
        });

        TransferResponseDTO response = transferService.transfer(request);

        assertEquals("SUCCESS", response.getStatus());
        assertEquals(400.0, fromAccount.getBalance());
        assertEquals(100.0, toAccount.getBalance());
        verify(accountRepository, times(2)).save(any(Account.class));
        verify(transactionLogRepository).save(any(TransactionLog.class));
    }

    @Test
    @DisplayName("Should successfully transfer large amount")
    void testTransferLargeAmount() {
        TransferRequestDTO request = new TransferRequestDTO();
        request.setFromAccountId("MTS2026-11111111");
        request.setToAccountId("MTS2026-22222222");
        request.setAmount(10000.50);
        request.setIdempotencyKey("key-large");

        Account fromAccount = createAccount("MTS2026-11111111", "Sender", 15000.0, "ACTIVE");
        Account toAccount = createAccount("MTS2026-22222222", "Receiver", 1000.0, "ACTIVE");

        when(transactionLogRepository.findByIdempotencyKey("key-large")).thenReturn(Optional.empty());
        when(accountRepository.findById("MTS2026-11111111")).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById("MTS2026-22222222")).thenReturn(Optional.of(toAccount));
        when(transactionLogRepository.save(any(TransactionLog.class))).thenAnswer(invocation -> {
            TransactionLog log = invocation.getArgument(0);
            log.setId(1L);
            return log;
        });

        TransferResponseDTO response = transferService.transfer(request);

        assertEquals("SUCCESS", response.getStatus());
        assertEquals(4999.50, fromAccount.getBalance());
        assertEquals(11000.50, toAccount.getBalance());
    }

    // ============ FAILURE SCENARIOS ============

    @Test
    @DisplayName("Should fail transfer when sender has insufficient balance")
    void testTransferInsufficientBalance() {
        TransferRequestDTO request = new TransferRequestDTO();
        request.setFromAccountId("MTS2026-11111111");
        request.setToAccountId("MTS2026-22222222");
        request.setAmount(1000.0);
        request.setIdempotencyKey("key-456");

        Account fromAccount = createAccount("MTS2026-11111111", "Sender", 100.0, "ACTIVE");
        Account toAccount = createAccount("MTS2026-22222222", "Receiver", 0.0, "ACTIVE");

        when(transactionLogRepository.findByIdempotencyKey("key-456")).thenReturn(Optional.empty());
        when(accountRepository.findById("MTS2026-11111111")).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById("MTS2026-22222222")).thenReturn(Optional.of(toAccount));

        TransferResponseDTO response = transferService.transfer(request);

        assertEquals("FAILED", response.getStatus());
        assertEquals(100.0, fromAccount.getBalance());
        assertEquals(0.0, toAccount.getBalance());
        verify(accountRepository, never()).save(any());
        verify(failureLogService).saveFailureLog(any(TransactionLog.class));
    }

    @Test
    @DisplayName("Should fail transfer when sender account is INACTIVE")
    void testTransferFromInactiveAccount() {
        TransferRequestDTO request = new TransferRequestDTO();
        request.setFromAccountId("MTS2026-11111111");
        request.setToAccountId("MTS2026-22222222");
        request.setAmount(100.0);
        request.setIdempotencyKey("key-789");

        Account fromAccount = createAccount("MTS2026-11111111", "Sender", 500.0, "INACTIVE");
        Account toAccount = createAccount("MTS2026-22222222", "Receiver", 0.0, "ACTIVE");

        when(transactionLogRepository.findByIdempotencyKey("key-789")).thenReturn(Optional.empty());
        when(accountRepository.findById("MTS2026-11111111")).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById("MTS2026-22222222")).thenReturn(Optional.of(toAccount));

        TransferResponseDTO response = transferService.transfer(request);

        assertEquals("FAILED", response.getStatus());
        verify(accountRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should fail transfer when receiver account is INACTIVE")
    void testTransferToInactiveAccount() {
        TransferRequestDTO request = new TransferRequestDTO();
        request.setFromAccountId("MTS2026-11111111");
        request.setToAccountId("MTS2026-22222222");
        request.setAmount(100.0);
        request.setIdempotencyKey("key-000");

        Account fromAccount = createAccount("MTS2026-11111111", "Sender", 500.0, "ACTIVE");
        Account toAccount = createAccount("MTS2026-22222222", "Receiver", 0.0, "INACTIVE");

        when(transactionLogRepository.findByIdempotencyKey("key-000")).thenReturn(Optional.empty());
        when(accountRepository.findById("MTS2026-11111111")).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById("MTS2026-22222222")).thenReturn(Optional.of(toAccount));

        TransferResponseDTO response = transferService.transfer(request);

        assertEquals("FAILED", response.getStatus());
        verify(accountRepository, never()).save(any());
    }

    // ============ EDGE CASES ============

    @Test
    @DisplayName("Should prevent transfer to same account")
    void testTransferToSameAccount() {
        TransferRequestDTO request = new TransferRequestDTO();
        request.setFromAccountId("MTS2026-11111111");
        request.setToAccountId("MTS2026-11111111");
        request.setAmount(100.0);
        request.setIdempotencyKey("key-same");

        Account account = createAccount("MTS2026-11111111", "User", 500.0, "ACTIVE");

        when(transactionLogRepository.findByIdempotencyKey("key-same")).thenReturn(Optional.empty());
        when(accountRepository.findById("MTS2026-11111111")).thenReturn(Optional.of(account));

        TransferResponseDTO response = transferService.transfer(request);

        assertEquals("FAILED", response.getStatus());
        assertTrue(response.getMessage().contains("same account"));
    }

    @Test
    @DisplayName("Should handle idempotent requests (duplicate transfer)")
    void testIdempotentTransfer() {
        TransferRequestDTO request = new TransferRequestDTO();
        request.setFromAccountId("MTS2026-11111111");
        request.setToAccountId("MTS2026-22222222");
        request.setAmount(100.0);
        request.setIdempotencyKey("key-dup");

        TransactionLog existingLog = new TransactionLog();
        existingLog.setId(1L);
        existingLog.setStatus("SUCCESS");

        when(transactionLogRepository.findByIdempotencyKey("key-dup")).thenReturn(Optional.of(existingLog));

        TransferResponseDTO response = transferService.transfer(request);

        assertEquals("FAILED", response.getStatus());
        verify(accountRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Should fail when sender account not found")
    void testTransferSenderNotFound() {
        TransferRequestDTO request = new TransferRequestDTO();
        request.setFromAccountId("NONEXISTENT");
        request.setToAccountId("MTS2026-22222222");
        request.setAmount(100.0);
        request.setIdempotencyKey("key-404-1");

        when(transactionLogRepository.findByIdempotencyKey("key-404-1")).thenReturn(Optional.empty());
        when(accountRepository.findById("NONEXISTENT")).thenReturn(Optional.empty());

        TransferResponseDTO response = transferService.transfer(request);

        assertEquals("FAILED", response.getStatus());
        assertTrue(response.getMessage().contains("not found"));
    }

    @Test
    @DisplayName("Should fail when receiver account not found")
    void testTransferReceiverNotFound() {
        TransferRequestDTO request = new TransferRequestDTO();
        request.setFromAccountId("MTS2026-11111111");
        request.setToAccountId("NONEXISTENT");
        request.setAmount(100.0);
        request.setIdempotencyKey("key-404-2");

        Account fromAccount = createAccount("MTS2026-11111111", "Sender", 500.0, "ACTIVE");

        when(transactionLogRepository.findByIdempotencyKey("key-404-2")).thenReturn(Optional.empty());
        when(accountRepository.findById("MTS2026-11111111")).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById("NONEXISTENT")).thenReturn(Optional.empty());

        TransferResponseDTO response = transferService.transfer(request);

        assertEquals("FAILED", response.getStatus());
    }

    @Test
    @DisplayName("Should reject zero amount transfer")
    void testTransferZeroAmount() {
        TransferRequestDTO request = new TransferRequestDTO();
        request.setFromAccountId("MTS2026-11111111");
        request.setToAccountId("MTS2026-22222222");
        request.setAmount(0.0);
        request.setIdempotencyKey("key-zero");

        Account fromAccount = createAccount("MTS2026-11111111", "Sender", 500.0, "ACTIVE");
        Account toAccount = createAccount("MTS2026-22222222", "Receiver", 0.0, "ACTIVE");

        when(transactionLogRepository.findByIdempotencyKey("key-zero")).thenReturn(Optional.empty());
        when(accountRepository.findById("MTS2026-11111111")).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById("MTS2026-22222222")).thenReturn(Optional.of(toAccount));

        TransferResponseDTO response = transferService.transfer(request);

        assertEquals("FAILED", response.getStatus());
    }

    @Test
    @DisplayName("Should reject negative amount transfer")
    void testTransferNegativeAmount() {
        TransferRequestDTO request = new TransferRequestDTO();
        request.setFromAccountId("MTS2026-11111111");
        request.setToAccountId("MTS2026-22222222");
        request.setAmount(-100.0);
        request.setIdempotencyKey("key-neg");

        Account fromAccount = createAccount("MTS2026-11111111", "Sender", 500.0, "ACTIVE");
        Account toAccount = createAccount("MTS2026-22222222", "Receiver", 0.0, "ACTIVE");

        when(transactionLogRepository.findByIdempotencyKey("key-neg")).thenReturn(Optional.empty());
        when(accountRepository.findById("MTS2026-11111111")).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById("MTS2026-22222222")).thenReturn(Optional.of(toAccount));

        TransferResponseDTO response = transferService.transfer(request);

        assertEquals("FAILED", response.getStatus());
    }

    // ============ TRANSACTION HISTORY TESTS ============

    @Test
    @DisplayName("Should retrieve transaction history for account")
    void testGetTransactionHistory() {
        String accountId = "MTS2026-11111111";

        TransactionLog log1 = createTransactionLog(1L, accountId, "MTS2026-22222222", 100.0, "SUCCESS");
        TransactionLog log2 = createTransactionLog(2L, "MTS2026-22222222", accountId, 50.0, "SUCCESS");

        when(transactionLogRepository.findTransactionHistory(accountId)).thenReturn(Arrays.asList(log1, log2));

        List<TransactionHistoryDTO> history = transferService.getTransactionHistory(accountId);

        assertEquals(2, history.size());
        assertEquals(100.0, history.get(0).amount());
        assertEquals(50.0, history.get(1).amount());
    }

    @Test
    @DisplayName("Should return empty transaction history when no transactions")
    void testGetTransactionHistoryEmpty() {
        String accountId = "MTS2026-11111111";

        when(transactionLogRepository.findTransactionHistory(accountId)).thenReturn(Arrays.asList());

        List<TransactionHistoryDTO> history = transferService.getTransactionHistory(accountId);

        assertTrue(history.isEmpty());
    }

    @Test
    @DisplayName("Should return multiple transactions in correct order")
    void testGetTransactionHistoryMultiple() {
        String accountId = "MTS2026-11111111";

        TransactionLog log1 = createTransactionLog(1L, accountId, "MTS2026-22222222", 100.0, "SUCCESS");
        TransactionLog log2 = createTransactionLog(2L, accountId, "MTS2026-33333333", 50.0, "SUCCESS");
        TransactionLog log3 = createTransactionLog(3L, "MTS2026-44444444", accountId, 200.0, "SUCCESS");

        when(transactionLogRepository.findTransactionHistory(accountId)).thenReturn(Arrays.asList(log1, log2, log3));

        List<TransactionHistoryDTO> history = transferService.getTransactionHistory(accountId);

        assertEquals(3, history.size());
        assertEquals(100.0, history.get(0).amount());
        assertEquals(50.0, history.get(1).amount());
        assertEquals(200.0, history.get(2).amount());
    }

    // ============ HELPER METHODS ============

    private Account createAccount(String id, String holderName, double balance, String status) {
        Account account = new Account();
        account.setId(id);
        account.setHolderName(holderName);
        account.setBalance(balance);
        account.setStatus(status);
        return account;
    }

    private TransactionLog createTransactionLog(Long id, String fromId, String toId, double amount, String status) {
        TransactionLog log = new TransactionLog();
        log.setId(id);
        log.setFromAccountId(fromId);
        log.setToAccountId(toId);
        log.setAmount(amount);
        log.setStatus(status);
        return log;
    }

    private TransactionHistoryDTO createTransactionHistoryDTO(Long id, String fromId, String toId, double amount, String status) {
        return new TransactionHistoryDTO(id, fromId, toId, amount, status, null, null);
    }
}