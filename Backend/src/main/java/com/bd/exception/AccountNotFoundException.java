package com.bd.exception;

public class AccountNotFoundException extends RuntimeException{
    public AccountNotFoundException(String id) {
        super("Account not found with ID: " + id);
    }
}
