package com.bd.service;

import com.bd.dto.LoginRequest;
import com.bd.dto.LoginResponse;
import com.bd.model.Account;
import com.bd.model.AppUser;
import com.bd.repository.AccountRepository;
import com.bd.repository.AppUserRepository;
import com.bd.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authService = new AuthService(accountRepository, appUserRepository, authenticationManager, jwtUtil, passwordEncoder);
    }

    // ============ LOGIN TESTS ============

    @Test
    @DisplayName("Login should succeed with valid credentials")
    void testLoginSuccess() {
        String username = "alice";
        String password = "password123";
        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword(password);

        AppUser appUser = new AppUser();
        appUser.setUsername(username);
        appUser.setAccountId("MTS2026-12345678");
        appUser.setDisplayName("Alice Holder");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(username, password));
        when(appUserRepository.findByUsername(username)).thenReturn(Optional.of(appUser));
        when(jwtUtil.generateToken(username)).thenReturn("jwt-token-123");

        Optional<LoginResponse> response = authService.login(request);

        assertTrue(response.isPresent());
        LoginResponse lr = response.get();
        assertTrue(lr.isAuthenticated());
        assertEquals("jwt-token-123", lr.getToken());
        assertEquals("MTS2026-12345678", lr.getUser().getId());
        assertEquals("Alice Holder", lr.getUser().getName());
    }

    @Test
    @DisplayName("Login should fail with invalid credentials")
    void testLoginFailsWithInvalidCredentials() {
        String username = "alice";
        String password = "wrongpassword";
        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword(password);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        Optional<LoginResponse> response = authService.login(request);

        assertFalse(response.isPresent());
    }

    @Test
    @DisplayName("Login should fail when user not found")
    void testLoginFailsWhenUserNotFound() {
        String username = "nonexistent";
        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword("password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(username, "password"));
        when(appUserRepository.findByUsername(username)).thenReturn(Optional.empty());

        Optional<LoginResponse> response = authService.login(request);

        assertFalse(response.isPresent());
    }

    @Test
    @DisplayName("Login should fail with null username")
    void testLoginFailsWithNullUsername() {
        LoginRequest request = new LoginRequest();
        request.setUsername(null);
        request.setPassword("password");

        Optional<LoginResponse> response = authService.login(request);

        assertFalse(response.isPresent());
    }

    @Test
    @DisplayName("Login should fail with blank username")
    void testLoginFailsWithBlankUsername() {
        LoginRequest request = new LoginRequest();
        request.setUsername("   ");
        request.setPassword("password");

        Optional<LoginResponse> response = authService.login(request);

        assertFalse(response.isPresent());
    }

    @Test
    @DisplayName("Login should fail with null request")
    void testLoginFailsWithNullRequest() {
        Optional<LoginResponse> response = authService.login(null);
        assertFalse(response.isPresent());
    }

    // ============ REGISTER TESTS ============

    @Test
    @DisplayName("Register should create user and account successfully")
    void testRegisterSuccess() {
        String username = "newuser";
        String password = "SecurePass@123";
        String holderName = "New User";

        Account account = new Account();
        account.setId("MTS2026-87654321");
        account.setHolderName(holderName);
        account.setBalance(0.0);
        account.setStatus("ACTIVE");

        when(appUserRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(accountRepository.save(any(Account.class))).thenReturn(account);
        when(appUserRepository.save(any(AppUser.class))).thenReturn(new AppUser());
        when(passwordEncoder.encode(password)).thenReturn("encoded-password");
        when(jwtUtil.generateToken(username)).thenReturn("jwt-token-456");

        Optional<LoginResponse> response = authService.register(username, password, holderName);

        assertTrue(response.isPresent());
        LoginResponse lr = response.get();
        assertTrue(lr.isAuthenticated());
        assertEquals("jwt-token-456", lr.getToken());
        assertEquals("MTS2026-87654321", lr.getUser().getId());
        assertEquals(holderName, lr.getUser().getName());

        verify(accountRepository).save(argThat(acc -> 
            acc.getHolderName().equals(holderName) && 
            acc.getBalance() == 0.0 && 
            "ACTIVE".equals(acc.getStatus())
        ));
        verify(appUserRepository).save(argThat(user -> 
            user.getUsername().equals(username) && 
            user.getAccountId().equals("MTS2026-87654321")
        ));
    }

    @Test
    @DisplayName("Register should fail when user already exists")
    void testRegisterFailsWhenUserExists() {
        String username = "existing";
        AppUser existingUser = new AppUser();
        existingUser.setUsername(username);

        when(appUserRepository.findByUsername(username)).thenReturn(Optional.of(existingUser));

        Optional<LoginResponse> response = authService.register(username, "password", "Name");

        assertFalse(response.isPresent());
        verify(accountRepository, never()).save(any());
        verify(appUserRepository, never()).save(any(AppUser.class));
    }

    @Test
    @DisplayName("Register should fail with null username")
    void testRegisterFailsWithNullUsername() {
        Optional<LoginResponse> response = authService.register(null, "password", "Name");
        assertFalse(response.isPresent());
    }

    @Test
    @DisplayName("Register should fail with null password")
    void testRegisterFailsWithNullPassword() {
        Optional<LoginResponse> response = authService.register("user", null, "Name");
        assertFalse(response.isPresent());
    }

    @Test
    @DisplayName("Register should fail with null holder name")
    void testRegisterFailsWithNullHolderName() {
        Optional<LoginResponse> response = authService.register("user", "password", null);
        assertFalse(response.isPresent());
    }

    @Test
    @DisplayName("Register should encode password before saving")
    void testRegisterEncodesPassword() {
        String username = "user";
        String rawPassword = "RawPassword123";
        String holderName = "User Name";
        Account account = new Account();
        account.setId("MTS2026-99999999");

        when(appUserRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(accountRepository.save(any(Account.class))).thenReturn(account);
        when(passwordEncoder.encode(rawPassword)).thenReturn("$2a$10$encoded");
        when(jwtUtil.generateToken(username)).thenReturn("token");

        authService.register(username, rawPassword, holderName);

        verify(passwordEncoder).encode(rawPassword);
        verify(appUserRepository).save(argThat(user -> 
            "$2a$10$encoded".equals(user.getPassword())
        ));
    }

    @Test
    @DisplayName("Register creates account with ACTIVE status and zero balance")
    void testRegisterCreatesActiveAccountWithZeroBalance() {
        String username = "activeuser";
        Account account = new Account();
        account.setId("MTS2026-11111111");

        when(appUserRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(accountRepository.save(any(Account.class))).thenReturn(account);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(jwtUtil.generateToken(username)).thenReturn("token");

        authService.register(username, "pass", "User Name");

        verify(accountRepository).save(argThat(acc -> 
            "ACTIVE".equals(acc.getStatus()) && acc.getBalance() == 0.0
        ));
    }

    // ============ USERNAME VALIDATION TESTS ============

    @Test
    @DisplayName("Register should fail with username too short (< 3 chars)")
    void testRegisterFailsWithShortUsername() {
        Optional<LoginResponse> response = authService.register("ab", "Pass@word123", "John Doe");
        assertFalse(response.isPresent());
    }

    @Test
    @DisplayName("Register should fail with username too long (> 32 chars)")
    void testRegisterFailsWithLongUsername() {
        String longUsername = "a".repeat(33);
        Optional<LoginResponse> response = authService.register(longUsername, "Pass@word123", "John Doe");
        assertFalse(response.isPresent());
    }

    @Test
    @DisplayName("Register should fail with invalid characters in username")
    void testRegisterFailsWithInvalidUsernameChars() {
        Optional<LoginResponse> response = authService.register("user@host", "Pass@word123", "John Doe");
        assertFalse(response.isPresent());
    }

    @Test
    @DisplayName("Register should succeed with valid username (alphanumeric, underscore, dot)")
    void testRegisterSucceedsWithValidUsername() {
        String username = "user.name_123";
        Account account = new Account();
        account.setId("MTS2026-11111111");

        when(appUserRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(accountRepository.save(any(Account.class))).thenReturn(account);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(jwtUtil.generateToken(username)).thenReturn("token");

        Optional<LoginResponse> response = authService.register(username, "Pass@word123", "John Doe");
        assertTrue(response.isPresent());
    }

    @Test
    @DisplayName("Register should fail when username has spaces")
    void testRegisterFailsUsernameWithSpaces() {
        Optional<LoginResponse> response = authService.register("user name", "Pass@word123", "John Doe");
        assertFalse(response.isPresent());
    }

    // ============ HOLDER NAME VALIDATION TESTS ============

    @Test
    @DisplayName("Register should fail with holder name too short (< 2 chars)")
    void testRegisterFailsWithShortHolderName() {
        when(appUserRepository.findByUsername("validuser")).thenReturn(Optional.empty());
        Optional<LoginResponse> response = authService.register("validuser", "Pass@word123", "A");
        assertFalse(response.isPresent());
    }

    @Test
    @DisplayName("Register should fail with holder name too long (> 100 chars)")
    void testRegisterFailsWithLongHolderName() {
        when(appUserRepository.findByUsername("validuser")).thenReturn(Optional.empty());
        String longName = "A".repeat(101);
        Optional<LoginResponse> response = authService.register("validuser", "Pass@word123", longName);
        assertFalse(response.isPresent());
    }

    @Test
    @DisplayName("Register should fail with invalid characters in holder name")
    void testRegisterFailsWithInvalidHolderNameChars() {
        when(appUserRepository.findByUsername("validuser")).thenReturn(Optional.empty());
        Optional<LoginResponse> response = authService.register("validuser", "Pass@word123", "John@Doe123");
        assertFalse(response.isPresent());
    }

    @Test
    @DisplayName("Register should succeed with valid holder name (letters, spaces, hyphens)")
    void testRegisterSucceedsWithValidHolderName() {
        String username = "validuser";
        String holderName = "John Michael O'Connor-Smith";
        Account account = new Account();
        account.setId("MTS2026-11111111");

        when(appUserRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(accountRepository.save(any(Account.class))).thenReturn(account);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(jwtUtil.generateToken(username)).thenReturn("token");

        Optional<LoginResponse> response = authService.register(username, "Pass@word123", holderName);
        assertTrue(response.isPresent());
    }

    @Test
    @DisplayName("Register should succeed with holder name containing accented characters")
    void testRegisterSucceedsWithAccentedHolderName() {
        String username = "validuser";
        String holderName = "José María";
        Account account = new Account();
        account.setId("MTS2026-11111111");

        when(appUserRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(accountRepository.save(any(Account.class))).thenReturn(account);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(jwtUtil.generateToken(username)).thenReturn("token");

        Optional<LoginResponse> response = authService.register(username, "Pass@word123", holderName);
        assertTrue(response.isPresent());
    }

    @Test
    @DisplayName("Register should fail with blank holder name")
    void testRegisterFailsWithBlankHolderName() {
        when(appUserRepository.findByUsername("validuser")).thenReturn(Optional.empty());
        Optional<LoginResponse> response = authService.register("validuser", "Pass@word123", "   ");
        assertFalse(response.isPresent());
    }
}