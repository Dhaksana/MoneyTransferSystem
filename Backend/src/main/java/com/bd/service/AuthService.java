// src/main/java/com/bd/service/AuthService.java
package com.bd.service;

import com.bd.dto.LoginRequest;
import com.bd.dto.LoginResponse;
import com.bd.model.Account;
import com.bd.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final AccountRepository accounts;

    public AuthService(AccountRepository accounts) {
        this.accounts = accounts;
    }

    /**
     * Accepts username as either:
     *  - numeric "id" of the account, OR
     *  - account holderName (case-insensitive)
     * Accepts any password.
     */
    public Optional<LoginResponse> login(LoginRequest req) {
        if (req == null || req.getUsername() == null || req.getUsername().isBlank()) {
            return Optional.empty();
        }

        Optional<Account> match = Optional.empty();
        // Try as numeric id
        try {
            Integer id = Integer.valueOf(req.getUsername().trim());
            match = accounts.findById(id);
        } catch (NumberFormatException ignore) { /* not numeric id */ }

        // Otherwise by holderName
        if (match.isEmpty()) {
            match = accounts.findByHolderNameIgnoreCase(req.getUsername().trim());
        }

        if (match.isEmpty()) {
            return Optional.empty();
        }

        Account acc = match.get();
        // Accept any password, but ensure account is ACTIVE (optional)
        if (!acc.isActive()) {
            return Optional.empty();
        }

        String token = "demo-" + UUID.randomUUID(); // simple demo token
        return Optional.of(new LoginResponse(
            true,
            token,
            new LoginResponse.UserInfo(acc.getId(), acc.getHolderName())
        ));
    }
}