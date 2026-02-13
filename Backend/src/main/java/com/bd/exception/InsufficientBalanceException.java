package com.bd.exception;

public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(double balance, double amount) {
        super("Insufficient balance. Available: " + balance + ", Requested: " + amount);
    }
}
