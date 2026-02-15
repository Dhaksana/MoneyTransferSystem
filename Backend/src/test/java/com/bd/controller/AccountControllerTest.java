package com.bd.controller;

import com.bd.dto.AccountDTO;
import com.bd.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("AccountController Tests")
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccountService accountService;

    @BeforeEach
    void setUp() {
        reset(accountService);
    }

    // ============ GET ACCOUNT TESTS ============

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    @DisplayName("Should get account by ID successfully")
    void testGetAccountByIdSuccess() throws Exception {
        String accountId = "MTS2026-11111111";
        
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setId(accountId);
        accountDTO.setHolderName("John Doe");
        accountDTO.setBalance(5000.0);
        accountDTO.setStatus("ACTIVE");

        when(accountService.getAccountById(accountId)).thenReturn(accountDTO);

        mockMvc.perform(get("/api/v1/accounts/" + accountId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(accountId))
                .andExpect(jsonPath("$.holderName").value("John Doe"))
                .andExpect(jsonPath("$.balance").value(5000.0))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(accountService).getAccountById(accountId);
    }

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    @DisplayName("Should return 404 when account not found")
    void testGetAccountNotFound() throws Exception {
        String accountId = "NONEXISTENT";

        when(accountService.getAccountById(accountId)).thenReturn(null);

        mockMvc.perform(get("/api/v1/accounts/" + accountId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should require authentication for get account")
    void testGetAccountRequiresAuth() throws Exception {
        String accountId = "MTS2026-11111111";

        mockMvc.perform(get("/api/v1/accounts/" + accountId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // ============ GET ACCOUNT BALANCE TESTS ============

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    @DisplayName("Should get account balance successfully")
    void testGetAccountBalanceSuccess() throws Exception {
        String accountId = "MTS2026-11111111";

        when(accountService.getBalance(accountId)).thenReturn(5000.0);

        mockMvc.perform(get("/api/v1/accounts/" + accountId + "/balance")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(accountService).getBalance(accountId);
    }

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    @DisplayName("Should return zero balance for new account")
    void testGetAccountBalanceZero() throws Exception {
        String accountId = "MTS2026-22222222";

        when(accountService.getBalance(accountId)).thenReturn(0.0);

        mockMvc.perform(get("/api/v1/accounts/" + accountId + "/balance")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // ============ GET ALL ACCOUNTS TESTS ============

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should get all accounts")
    void testGetAllAccountsSuccess() throws Exception {
        AccountDTO dto1 = new AccountDTO();
        dto1.setId("MTS2026-11111111");
        dto1.setHolderName("User1");
        dto1.setBalance(1000.0);
        
        AccountDTO dto2 = new AccountDTO();
        dto2.setId("MTS2026-22222222");
        dto2.setHolderName("User2");
        dto2.setBalance(2000.0);

        List<AccountDTO> accounts = Arrays.asList(dto1, dto2);
        when(accountService.getAllAccounts()).thenReturn(accounts);

        mockMvc.perform(get("/api/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(accountService).getAllAccounts();
    }

    @Test
    @DisplayName("Should require authentication for get all accounts")
    void testGetAllAccountsRequiresAuth() throws Exception {
        mockMvc.perform(get("/api/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // ============ CREATE ACCOUNT TESTS ============

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should create account successfully")
    void testCreateAccountSuccess() throws Exception {
        AccountDTO requestDTO = new AccountDTO();
        requestDTO.setHolderName("Jane Doe");
        requestDTO.setBalance(1000.0);
        requestDTO.setStatus("ACTIVE");

        AccountDTO responseDTO = new AccountDTO();
        responseDTO.setId("MTS2026-33333333");
        responseDTO.setHolderName("Jane Doe");
        responseDTO.setBalance(1000.0);
        responseDTO.setStatus("ACTIVE");

        when(accountService.createAccount(any(AccountDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("MTS2026-33333333"))
                .andExpect(jsonPath("$.holderName").value("Jane Doe"));

        verify(accountService).createAccount(any(AccountDTO.class));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should create account with zero initial balance")
    void testCreateAccountZeroBalance() throws Exception {
        AccountDTO requestDTO = new AccountDTO();
        requestDTO.setHolderName("New User");
        requestDTO.setBalance(0.0);
        requestDTO.setStatus("ACTIVE");

        AccountDTO responseDTO = new AccountDTO();
        responseDTO.setId("MTS2026-44444444");
        responseDTO.setHolderName("New User");
        responseDTO.setBalance(0.0);
        responseDTO.setStatus("ACTIVE");

        when(accountService.createAccount(any(AccountDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.balance").value(0.0));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("Should deny create account for non-admin")
    void testCreateAccountDenyNonAdmin() throws Exception {
        AccountDTO requestDTO = new AccountDTO();
        requestDTO.setHolderName("Another User");
        requestDTO.setBalance(500.0);
        requestDTO.setStatus("ACTIVE");

        mockMvc.perform(post("/api/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isForbidden());

        verify(accountService, never()).createAccount(any(AccountDTO.class));
    }

    @Test
    @DisplayName("Should require authentication for create account")
    void testCreateAccountRequiresAuth() throws Exception {
        AccountDTO requestDTO = new AccountDTO();
        requestDTO.setHolderName("Some User");
        requestDTO.setBalance(1000.0);

        mockMvc.perform(post("/api/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isUnauthorized());
    }

    // ============ VERIFY ACCOUNT TESTS ============

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    @DisplayName("Should verify account exists")
    void testVerifyAccountExists() throws Exception {
        String accountId = "MTS2026-11111111";

        when(accountService.accountExists(accountId)).thenReturn(true);

        mockMvc.perform(get("/api/v1/accounts/" + accountId + "/exists")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(accountService).accountExists(accountId);
    }

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    @DisplayName("Should verify account does not exist")
    void testVerifyAccountNotExists() throws Exception {
        String accountId = "NONEXISTENT";

        when(accountService.accountExists(accountId)).thenReturn(false);

        mockMvc.perform(get("/api/v1/accounts/" + accountId + "/exists")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // ============ EDGE CASES ============

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    @DisplayName("Should handle account with special characters in holder name")
    void testAccountSpecialCharacters() throws Exception {
        String accountId = "MTS2026-55555555";
        
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setId(accountId);
        accountDTO.setHolderName("José María O'Connor-Smith");
        accountDTO.setBalance(2500.0);
        accountDTO.setStatus("ACTIVE");

        when(accountService.getAccountById(accountId)).thenReturn(accountDTO);

        mockMvc.perform(get("/api/v1/accounts/" + accountId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.holderName").value("José María O'Connor-Smith"));
    }

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    @DisplayName("Should get account with large balance")
    void testAccountLargeBalance() throws Exception {
        String accountId = "MTS2026-66666666";
        
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setId(accountId);
        accountDTO.setHolderName("Rich User");
        accountDTO.setBalance(999999999.99);
        accountDTO.setStatus("ACTIVE");

        when(accountService.getAccountById(accountId)).thenReturn(accountDTO);

        mockMvc.perform(get("/api/v1/accounts/" + accountId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(999999999.99));
    }

    // ============ ADMIN UPDATE TESTS ============

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Admin should update account balance successfully")
    void testAdminUpdateBalance() throws Exception {
        String accountId = "MTS2026-11111111";

        AccountDTO responseDTO = new AccountDTO();
        responseDTO.setId(accountId);
        responseDTO.setHolderName("John Doe");
        responseDTO.setBalance(5000.0);
        responseDTO.setStatus("ACTIVE");

        when(accountService.updateAccount(eq(accountId), any(AccountDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(put("/api/v1/accounts/" + accountId + "/balance")
                .param("balance", "5000.0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(5000.0));

        verify(accountService).updateAccount(eq(accountId), any(AccountDTO.class));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("Non-admin user should not be able to update balance")
    void testNonAdminCannotUpdateBalance() throws Exception {
        String accountId = "MTS2026-11111111";

        mockMvc.perform(put("/api/v1/accounts/" + accountId + "/balance")
                .param("balance", "5000.0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(accountService, never()).updateAccount(any(), any());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Admin should not update account with negative balance")
    void testAdminCannotUpdateNegativeBalance() throws Exception {
        String accountId = "MTS2026-11111111";

        mockMvc.perform(put("/api/v1/accounts/" + accountId + "/balance")
                .param("balance", "-100.0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

        verify(accountService, never()).updateAccount(any(), any());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Admin should update account information")
    void testAdminUpdateAccount() throws Exception {
        String accountId = "MTS2026-11111111";

        AccountDTO requestDTO = new AccountDTO();
        requestDTO.setHolderName("Updated Name");
        requestDTO.setStatus("INACTIVE");

        AccountDTO responseDTO = new AccountDTO();
        responseDTO.setId(accountId);
        responseDTO.setHolderName("Updated Name");
        responseDTO.setStatus("INACTIVE");
        responseDTO.setBalance(1000.0);

        when(accountService.updateAccount(eq(accountId), any(AccountDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(put("/api/v1/accounts/" + accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.holderName").value("Updated Name"))
                .andExpect(jsonPath("$.status").value("INACTIVE"));

        verify(accountService).updateAccount(eq(accountId), any(AccountDTO.class));
    }
}