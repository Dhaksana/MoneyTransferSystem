package com.bd.service;

import com.bd.dto.AccountDTO;
import com.bd.model.Account;
import com.bd.repository.AccountRepository;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("AccountService Tests")
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    private AccountService accountService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        accountService = new AccountService(accountRepository);
    }

    // ============ CREATE ACCOUNT TESTS ============

    @Test
    @DisplayName("Should create account successfully")
    void testCreateAccountSuccess() {
        AccountDTO dto = new AccountDTO();
        dto.setHolderName("John Doe");
        dto.setBalance(1000.0);
        dto.setStatus("ACTIVE");

        Account savedAccount = new Account();
        savedAccount.setId("MTS2026-12345678");
        savedAccount.setHolderName("John Doe");
        savedAccount.setBalance(1000.0);
        savedAccount.setStatus("ACTIVE");

        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);
        when(accountRepository.existsById(anyString())).thenReturn(false);

        AccountDTO result = accountService.createAccount(dto);

        assertNotNull(result);
        assertEquals("MTS2026-12345678", result.getId());
        assertEquals("John Doe", result.getHolderName());
        assertEquals(1000.0, result.getBalance());
        assertEquals("ACTIVE", result.getStatus());
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    @DisplayName("Should generate unique account ID when creating account without ID")
    void testCreateAccountGeneratesUniqueId() {
        AccountDTO dto = new AccountDTO();
        dto.setHolderName("Jane Doe");
        dto.setBalance(500.0);
        dto.setStatus("ACTIVE");

        Account saved = new Account();
        saved.setId("MTS2026-87654321");
        saved.setHolderName("Jane Doe");
        saved.setBalance(500.0);
        saved.setStatus("ACTIVE");

        when(accountRepository.existsById(anyString())).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenReturn(saved);

        AccountDTO result = accountService.createAccount(dto);

        assertNotNull(result.getId());
        assertTrue(result.getId().startsWith("MTS"));
    }

    // ============ GET ACCOUNT BY ID TESTS ============

    @Test
    @DisplayName("Should retrieve account by ID successfully")
    void testGetAccountByIdSuccess() {
        String accountId = "MTS2026-12345678";
        Account account = new Account();
        account.setId(accountId);
        account.setHolderName("Alice");
        account.setBalance(5000.0);
        account.setStatus("ACTIVE");

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        AccountDTO result = accountService.getAccountById(accountId);

        assertNotNull(result);
        assertEquals(accountId, result.getId());
        assertEquals("Alice", result.getHolderName());
        assertEquals(5000.0, result.getBalance());
    }

    @Test
    @DisplayName("Should throw exception when account not found by ID")
    void testGetAccountByIdNotFound() {
        String accountId = "NONEXISTENT";

        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> accountService.getAccountById(accountId));
        assertTrue(exception.getMessage().contains("Account not found"));
        verify(accountRepository).findById(accountId);
    }

    // ============ GET BALANCE TESTS ============

    @Test
    @DisplayName("Should retrieve balance successfully")
    void testGetBalanceSuccess() {
        String accountId = "MTS2026-12345678";
        Account account = new Account();
        account.setId(accountId);
        account.setBalance(7500.50);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        Double balance = accountService.getBalance(accountId);

        assertEquals(7500.50, balance);
    }

    @Test
    @DisplayName("Should throw exception when account not found for balance")
    void testGetBalanceAccountNotFound() {
        String accountId = "NONEXISTENT";

        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> accountService.getBalance(accountId));
    }

    @Test
    @DisplayName("Should return zero balance correctly")
    void testGetBalanceZero() {
        Account account = new Account();
        account.setId("MTS2026-00000000");
        account.setBalance(0.0);

        when(accountRepository.findById("MTS2026-00000000")).thenReturn(Optional.of(account));

        Double balance = accountService.getBalance("MTS2026-00000000");

        assertEquals(0.0, balance);
    }

    @Test
    @DisplayName("Should handle very large balance")
    void testLargeBalance() {
        Account account = new Account();
        account.setId("MTS2026-99999999");
        account.setBalance(999999999999.99);

        when(accountRepository.findById("MTS2026-99999999")).thenReturn(Optional.of(account));

        Double balance = accountService.getBalance("MTS2026-99999999");

        assertEquals(999999999999.99, balance);
    }

    // ============ GET ALL ACCOUNTS TESTS ============

    @Test
    @DisplayName("Should retrieve all accounts successfully")
    void testGetAllAccountsSuccess() {
        Account acc1 = new Account();
        acc1.setId("MTS2026-11111111");
        acc1.setHolderName("User 1");
        acc1.setBalance(1000.0);
        acc1.setStatus("ACTIVE");

        Account acc2 = new Account();
        acc2.setId("MTS2026-22222222");
        acc2.setHolderName("User 2");
        acc2.setBalance(2000.0);
        acc2.setStatus("ACTIVE");

        when(accountRepository.findAll()).thenReturn(Arrays.asList(acc1, acc2));

        List<AccountDTO> result = accountService.getAllAccounts();

        assertEquals(2, result.size());
        assertEquals("User 1", result.get(0).getHolderName());
        assertEquals("User 2", result.get(1).getHolderName());
    }

    @Test
    @DisplayName("Should return empty list when no accounts exist")
    void testGetAllAccountsEmpty() {
        when(accountRepository.findAll()).thenReturn(Arrays.asList());

        List<AccountDTO> result = accountService.getAllAccounts();

        assertTrue(result.isEmpty());
    }

    // ============ ACCOUNT EXISTS TESTS ============

    @Test
    @DisplayName("Should return true when account exists")
    void testAccountExistsTrue() {
        String accountId = "MTS2026-12345678";

        when(accountRepository.existsById(accountId)).thenReturn(true);

        boolean exists = accountService.accountExists(accountId);

        assertTrue(exists);
    }

    @Test
    @DisplayName("Should return false when account does not exist")
    void testAccountExistsFalse() {
        String accountId = "NONEXISTENT";

        when(accountRepository.existsById(accountId)).thenReturn(false);

        boolean exists = accountService.accountExists(accountId);

        assertFalse(exists);
    }

    // ============ EDGE CASES ============

    @Test
    @DisplayName("Should handle different account statuses")
    void testDifferentStatuses() {
        String [] statuses = {"ACTIVE", "INACTIVE", "BLOCKED"};

        for (String status : statuses) {
            Account account = new Account();
            account.setId("MTS2026-12345678");
            account.setStatus(status);

            when(accountRepository.findById("MTS2026-12345678")).thenReturn(Optional.of(account));

            AccountDTO result = accountService.getAccountById("MTS2026-12345678");

            assertEquals(status, result.getStatus());
        }
    }
}