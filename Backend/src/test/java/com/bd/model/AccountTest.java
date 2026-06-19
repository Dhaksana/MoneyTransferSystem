package com.bd.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.bd.exception.InactiveAccountException;
import com.bd.exception.InsufficientBalanceException;

public class AccountTest {

    @Test
    public void testDebitSuccess() {
        Account account = new Account();
        account.setBalance(1000);
        account.setStatus("ACTIVE");
        account.debit(500);
        assertEquals(500, account.getBalance());
    }

    @Test
    public void testDebitInsufficientBalance() {
        Account account = new Account();
        account.setBalance(100);
        account.setStatus("ACTIVE");
        assertThrows(InsufficientBalanceException.class, () -> account.debit(200));
    }

    @Test
    public void testDebitInactiveAccount() {
        Account account = new Account();
        account.setBalance(1000);
        account.setStatus("INACTIVE");
        assertThrows(InactiveAccountException.class, () -> account.debit(100));
    }

    @Test
    public void testDebitBlockedAccount() {
        Account account = new Account();
        account.setBalance(1000);
        account.setStatus("BLOCKED");
        assertThrows(InactiveAccountException.class, () -> account.debit(100));
    }

    @Test
    public void testDebitNegativeAmount() {
        Account account = new Account();
        account.setBalance(1000);
        account.setStatus("ACTIVE");
        assertThrows(IllegalArgumentException.class, () -> account.debit(-100));
    }

    @Test
    public void testDebitZeroAmount() {
        Account account = new Account();
        account.setBalance(1000);
        account.setStatus("ACTIVE");
        assertThrows(IllegalArgumentException.class, () -> account.debit(0));
    }

    @Test
    public void testCreditSuccess() {
        Account account = new Account();
        account.setBalance(500);
        account.setStatus("ACTIVE");
        account.credit(300);
        assertEquals(800, account.getBalance());
    }

    @Test
    public void testCreditInactiveAccount() {
        Account account = new Account();
        account.setBalance(500);
        account.setStatus("INACTIVE");
        assertThrows(InactiveAccountException.class, () -> account.credit(100));
    }

    @Test
    public void testCreditNegativeAmount() {
        Account account = new Account();
        account.setBalance(500);
        account.setStatus("ACTIVE");
        assertThrows(IllegalArgumentException.class, () -> account.credit(-50));
    }

    @Test
    public void testCreditZeroAmount() {
        Account account = new Account();
        account.setBalance(500);
        account.setStatus("ACTIVE");
        assertThrows(IllegalArgumentException.class, () -> account.credit(0));
    }

    @Test
    public void testIsActive() {
        Account account = new Account();
        account.setStatus("ACTIVE");
        assertTrue(account.isActive());
        account.setStatus("INACTIVE");
        assertFalse(account.isActive());
        account.setStatus("BLOCKED");
        assertFalse(account.isActive());
    }

    @Test
    public void testMultipleTransactions() {
        Account account = new Account();
        account.setBalance(500);
        account.setStatus("ACTIVE");
        account.debit(200);
        account.credit(100);
        account.debit(50);
        assertEquals(350, account.getBalance());
    }

}
