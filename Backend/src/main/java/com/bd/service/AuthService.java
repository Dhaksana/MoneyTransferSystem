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
    private final AuditService auditService;

    public AuthService(AccountRepository accounts,
                       AppUserRepository users,
                       AuthenticationManager authManager,
                       JwtUtil jwtUtil,
                       PasswordEncoder passwordEncoder,
                       AuditService auditService) {
        this.accounts = accounts;
        this.users = users;
        this.authManager = authManager;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    public Optional<LoginResponse> login(LoginRequest req) {
        if (req == null || req.getUsername() == null || req.getUsername().isBlank()) {
            return Optional.empty();
        }

        try {
            authManager.authenticate(new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
        } catch (Exception ex) {
            if (req.getUsername() != null && !req.getUsername().isBlank()) {
                auditService.log(req.getUsername(), "LOGIN_FAILED", "Invalid credentials or inactive account");
            }
            return Optional.empty();
        }

        AppUser u = users.findByUsername(req.getUsername()).orElse(null);
        if (u == null) return Optional.empty();
        if (!"ACTIVE".equalsIgnoreCase(u.getStatus())) return Optional.empty();

        auditService.log(req.getUsername(), "LOGIN_SUCCESS", "User logged in successfully");

        String token = jwtUtil.generateToken(u.getUsername());

        String accountId = u.getAccountId();
        String displayName = u.getDisplayName();

        return Optional.of(new LoginResponse(true, token, new LoginResponse.UserInfo(accountId, displayName, u.getRole())));
    }

    /**
     * Register a new application user and create a linked Account.
     * Returns LoginResponse with token on success.
     */
    public Optional<LoginResponse> register(String username, String rawPassword, String holderName) {
        if (username == null || rawPassword == null || holderName == null) return Optional.empty();
        // enforce password strength: min 8 chars, at least one uppercase and one symbol
        if (!isStrongPassword(rawPassword)) return Optional.empty();
        if (users.findByUsername(username).isPresent()) return Optional.empty();

        AppUser u = new AppUser();
        u.setUsername(username);
        u.setFullName(holderName);
        u.setEmail(toRegistrationEmail(username));
        u.setPassword(passwordEncoder.encode(rawPassword));
        u.setRole("USER");
        u.setStatus("ACTIVE");
        u.setDisplayName(holderName);
        AppUser savedUser = users.save(u);

        Account acc = new Account();
        acc.setId(generateUniqueAccountId());
        acc.setAccountNumber(acc.getId());
        acc.setAccountType("SAVINGS");
        acc.setHolderName(holderName);
        acc.setBalance(0.0);
        acc.setStatus("ACTIVE");
        acc.setUser(savedUser);
        Account saved = accounts.save(acc);

        u.setAccountId(saved.getId());
        users.save(u);

        auditService.log(u.getUsername(), "REGISTER", "User registered with account " + saved.getId());

        String token = jwtUtil.generateToken(u.getUsername());
        return Optional.of(new LoginResponse(true, token, new LoginResponse.UserInfo(saved.getId(), holderName, u.getRole())));
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

    private String toRegistrationEmail(String username) {
        if (username.contains("@")) {
            return username;
        }
        return username + "@mts.local";
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
