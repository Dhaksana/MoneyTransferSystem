package com.bd.exception;

public class InactiveAccountException extends RuntimeException {
    public InactiveAccountException() {
        super("Account is not ACTIVE");
    }
}
