package com.bd.exception;

import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ---------- Account Not Found (404) ----------
    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleAccountNotFound(
            AccountNotFoundException ex) {

        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // ---------- Business Rule Errors (400) ----------
    @ExceptionHandler({
            InactiveAccountException.class,
            InsufficientBalanceException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<Map<String, Object>> handleBusinessExceptions(
            RuntimeException ex) {

        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // ---------- Validation Errors (400) ----------
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error ->
                        errors.put(error.getField(), error.getDefaultMessage()));

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("errors", errors);

        return ResponseEntity.badRequest().body(body);
    }

    // ---------- Fallback (500) ----------
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(
            Exception ex) {

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Unexpected error occurred"
        );
    }

    // ---------- Utility Method ----------
    private ResponseEntity<Map<String, Object>> buildResponse(
            HttpStatus status, String message) {

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);

        return ResponseEntity.status(status).body(body);
    }
}
