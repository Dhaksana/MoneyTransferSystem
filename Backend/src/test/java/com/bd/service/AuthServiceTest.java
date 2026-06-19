package com.bd.service;

import com.bd.dto.LoginRequest;
import com.bd.dto.LoginResponse;
import com.bd.model.Account;
import com.bd.model.AppUser;
import com.bd.repository.AccountRepository;
import com.bd.repository.AppUserRepository;
import com.bd.security.JwtUtil;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AccountRepository accounts;

    @Mock
    private AppUserRepository users;

    @Mock
    private AuthenticationManager authManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AuthService authService;

    @Test
    void testLoginSuccess() {
        LoginRequest req = new LoginRequest();
        req.setUsername("testuser");
        req.setPassword("pass");

        AppUser user = new AppUser();
        user.setUsername("testuser");
        user.setStatus("ACTIVE");
        user.setDisplayName("Test User");
        user.setRole("USER");
        user.setAccountId("acc-1");

        when(authManager.authenticate(any())).thenReturn(mock(Authentication.class));
        when(users.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("testuser")).thenReturn("token");

        Optional<LoginResponse> result = authService.login(req);

        assertTrue(result.isPresent());
        assertTrue(result.get().isAuthenticated());
        assertEquals("token", result.get().getToken());
        verify(auditService).log("testuser", "LOGIN_SUCCESS", "User logged in successfully");
    }

    @Test
    void testLoginInvalidCredentials() {
        LoginRequest req = new LoginRequest();
        req.setUsername("testuser");
        req.setPassword("wrong");

        doThrow(new BadCredentialsException("bad creds")).when(authManager).authenticate(any());

        Optional<LoginResponse> result = authService.login(req);

        assertTrue(result.isEmpty());
    }

    @Test
    void testLoginInactiveUser() {
        LoginRequest req = new LoginRequest();
        req.setUsername("testuser");
        req.setPassword("pass");

        AppUser user = new AppUser();
        user.setUsername("testuser");
        user.setStatus("BLOCKED");

        when(authManager.authenticate(any())).thenReturn(mock(Authentication.class));
        when(users.findByUsername("testuser")).thenReturn(Optional.of(user));

        Optional<LoginResponse> result = authService.login(req);

        assertTrue(result.isEmpty());
    }

    @Test
    void testLoginNullRequest() {
        Optional<LoginResponse> result = authService.login(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void testRegisterSuccess() {
        String username = "newuser";
        String password = "Strong@1";
        String holderName = "New User";

        AppUser savedUser = new AppUser();
        savedUser.setId(1L);
        savedUser.setUsername(username);
        savedUser.setFullName(holderName);
        savedUser.setRole("USER");
        savedUser.setStatus("ACTIVE");
        savedUser.setDisplayName(holderName);

        Account savedAccount = new Account();
        savedAccount.setId("MTS2026-12345678");
        savedAccount.setAccountNumber("MTS2026-12345678");
        savedAccount.setHolderName(holderName);

        when(users.findByUsername(username)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn("encoded");
        when(users.save(any(AppUser.class))).thenReturn(savedUser);
        when(accounts.save(any(Account.class))).thenReturn(savedAccount);
        when(jwtUtil.generateToken(username)).thenReturn("token");

        Optional<LoginResponse> result = authService.register(username, password, holderName);

        assertTrue(result.isPresent());
        assertTrue(result.get().isAuthenticated());
        assertEquals("token", result.get().getToken());
        verify(auditService).log(eq(username), eq("REGISTER"), anyString());
    }

    @Test
    void testRegisterWeakPassword() {
        String username = "newuser";
        String password = "short";
        String holderName = "New User";

        Optional<LoginResponse> result = authService.register(username, password, holderName);
        assertTrue(result.isEmpty());
    }

    @Test
    void testRegisterNoUppercase() {
        String username = "newuser";
        String password = "nouppercase1!";
        String holderName = "New User";

        Optional<LoginResponse> result = authService.register(username, password, holderName);
        assertTrue(result.isEmpty());
    }

    @Test
    void testRegisterNoSymbol() {
        String username = "newuser";
        String password = "NoSymbol12345";
        String holderName = "New User";

        Optional<LoginResponse> result = authService.register(username, password, holderName);
        assertTrue(result.isEmpty());
    }

    @Test
    void testRegisterDuplicateUsername() {
        String username = "existing";
        String password = "Strong@1";
        String holderName = "Existing User";

        when(users.findByUsername(username)).thenReturn(Optional.of(new AppUser()));

        Optional<LoginResponse> result = authService.register(username, password, holderName);
        assertTrue(result.isEmpty());
    }

    @Test
    void testRegisterNullParams() {
        Optional<LoginResponse> result = authService.register(null, null, null);
        assertTrue(result.isEmpty());
    }
}
