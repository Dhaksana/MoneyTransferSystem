// src/main/java/com/bd/service/AuthService.java
package com.bd.service;

import com.bd.dto.LoginRequest;
import com.bd.dto.LoginResponse;
import com.bd.model.Account;
import com.bd.model.AppUser;
import com.bd.repository.AccountRepository;
import com.bd.repository.AppUserRepository;
import com.bd.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class AuthService {

    private final AccountRepository accounts;
    private final AppUserRepository users;
    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AccountRepository accounts,
                       AppUserRepository users,
                       AuthenticationManager authManager,
                       JwtUtil jwtUtil,
                       PasswordEncoder passwordEncoder) {
        this.accounts = accounts;
        this.users = users;
        this.authManager = authManager;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<LoginResponse> login(LoginRequest req) {
        if (req == null || req.getUsername() == null || req.getUsername().isBlank()) {
            return Optional.empty();
        }

        try {
            authManager.authenticate(new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
        } catch (Exception ex) {
            return Optional.empty();
        }

        AppUser u = users.findByUsername(req.getUsername()).orElse(null);
        if (u == null) return Optional.empty();

        String token = jwtUtil.generateToken(u.getUsername());

        String accountId = u.getAccountId();
        String displayName = u.getDisplayName();
        String role = u.getRole();

        return Optional.of(new LoginResponse(true, token, new LoginResponse.UserInfo(accountId, displayName), role));
    }

    /**
     * Register a new application user and create a linked Account.
     * Returns LoginResponse with token on success.
     * Validation rules:
     * - Username: 3-32 chars, alphanumeric + underscore/dot only, must be unique
     * - Password: min 8 chars, at least one uppercase and one symbol
     * - Holder name: 2-100 chars, letters/spaces/hyphens only
     */
    public Optional<LoginResponse> register(String username, String rawPassword, String holderName) {
        // Null checks
        if (username == null || rawPassword == null || holderName == null) {
            return Optional.empty();
        }
        
        // Validate username
        if (!isValidUsername(username)) {
            return Optional.empty();
        }
        
        // Check username uniqueness
        if (users.findByUsername(username).isPresent()) {
            return Optional.empty(); // Username already exists
        }
        
        // Validate password strength
        if (!isStrongPassword(rawPassword)) {
            return Optional.empty();
        }
        
        // Validate holder name
        if (!isValidHolderName(holderName)) {
            return Optional.empty();
        }

        // Create bank Account with generated string id
        Account acc = new Account();
        acc.setId(generateUniqueAccountId());
        acc.setHolderName(holderName);
        acc.setBalance(0.0);
        acc.setStatus("ACTIVE");
        Account saved = accounts.save(acc);

        // Create AppUser
        AppUser u = new AppUser();
        u.setUsername(username);
        u.setPassword(passwordEncoder.encode(rawPassword));
        u.setAccountId(saved.getId());
        u.setDisplayName(holderName);
        users.save(u);

        String token = jwtUtil.generateToken(u.getUsername());
        return Optional.of(new LoginResponse(true, token, new LoginResponse.UserInfo(saved.getId(), holderName), u.getRole()));
    }

    /**
     * Validate username format and length
     * Rules: 3-32 characters, alphanumeric with underscore and dot allowed
     */
    private boolean isValidUsername(String username) {
        if (username == null || username.isBlank()) {
            return false;
        }
        if (username.length() < 3 || username.length() > 32) {
            return false;
        }
        // Allow alphanumeric, underscore, and dot
        return username.matches("^[a-zA-Z0-9._]+$");
    }

    /**
     * Validate holder name format and length
     * Rules: 2-100 characters, letters, spaces, hyphens, and apostrophes only
     */
    private boolean isValidHolderName(String holderName) {
        if (holderName == null || holderName.isBlank()) {
            return false;
        }
        if (holderName.length() < 2 || holderName.length() > 100) {
            return false;
        }
        // Allow letters, spaces, hyphens, apostrophes, and accented characters
        return holderName.matches("^[a-zA-Z\\s\\-'àáâäãåèéêëìíîïòóôöõùúûüœæńńşßÀÁÂÄÃÅÈÉÊËÌÍÎÏÒÓÔÖÕÙÚÛÜŒÆ]+$");
    }

    private boolean isStrongPassword(String pwd) {
        if (pwd == null) return false;
        if (pwd.length() < 8) return false;
        boolean hasUpper = pwd.chars().anyMatch(ch -> Character.isUpperCase(ch));
        if (!hasUpper) return false;
        boolean hasSymbol = pwd.chars().anyMatch(ch -> !Character.isLetterOrDigit(ch));
        if (!hasSymbol) return false;
        return true;
    }

    private final SecureRandom rnd = new SecureRandom();

    private String generateUniqueAccountId() {
        String candidate;
        int attempts = 0;
        do {
            String year = String.valueOf(LocalDate.now().getYear());
            int part = 10000000 + rnd.nextInt(90000000);
            candidate = "MTS" + year + "-" + part;
            attempts++;
            if (attempts > 20) break;
        } while (accounts.existsById(candidate));
        return candidate;
    }
}