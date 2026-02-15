package com.bd.controller;

import com.bd.dto.TransactionHistoryDTO;
import com.bd.dto.TransferRequestDTO;
import com.bd.dto.TransferResponseDTO;
import com.bd.service.TransferService;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("TransferController Tests")
class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransferService transferService;

    @BeforeEach
    void setUp() {
        reset(transferService);
    }

    // ============ TRANSFER ENDPOINT TESTS ============

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    @DisplayName("Should complete transfer successfully")
    void testTransferSuccess() throws Exception {
        TransferRequestDTO request = new TransferRequestDTO();
        request.setFromAccountId("MTS2026-11111111");
        request.setToAccountId("MTS2026-22222222");
        request.setAmount(100.0);
        request.setIdempotencyKey("key-123");

        TransferResponseDTO response = new TransferResponseDTO(1L, "SUCCESS", "Transfer completed successfully");

        when(transferService.transfer(any(TransferRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Transfer completed successfully"));

        verify(transferService).transfer(any(TransferRequestDTO.class));
    }

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    @DisplayName("Should fail transfer with insufficient balance")
    void testTransferInsufficientBalance() throws Exception {
        TransferRequestDTO request = new TransferRequestDTO();
        request.setFromAccountId("MTS2026-11111111");
        request.setToAccountId("MTS2026-22222222");
        request.setAmount(10000.0);
        request.setIdempotencyKey("key-456");

        TransferResponseDTO response = new TransferResponseDTO(null, "FAILED", "Insufficient balance");

        when(transferService.transfer(any(TransferRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FAILED"));
    }

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    @DisplayName("Should reject transfer to same account")
    void testTransferToSameAccount() throws Exception {
        TransferRequestDTO request = new TransferRequestDTO();
        request.setFromAccountId("MTS2026-11111111");
        request.setToAccountId("MTS2026-11111111");
        request.setAmount(100.0);
        request.setIdempotencyKey("key-same");

        TransferResponseDTO response = new TransferResponseDTO(null, "FAILED", "Cannot transfer to same account");

        when(transferService.transfer(any(TransferRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FAILED"));
    }

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    @DisplayName("Should reject duplicate transfer (idempotency)")
    void testTransferDuplicate() throws Exception {
        TransferRequestDTO request = new TransferRequestDTO();
        request.setFromAccountId("MTS2026-11111111");
        request.setToAccountId("MTS2026-22222222");
        request.setAmount(100.0);
        request.setIdempotencyKey("key-dup");

        TransferResponseDTO response = new TransferResponseDTO(null, "FAILED", "Duplicate transfer request");

        when(transferService.transfer(any(TransferRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FAILED"));
    }

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    @DisplayName("Should reject transfer with zero amount")
    void testTransferZeroAmount() throws Exception {
        TransferRequestDTO request = new TransferRequestDTO();
        request.setFromAccountId("MTS2026-11111111");
        request.setToAccountId("MTS2026-22222222");
        request.setAmount(0.0);
        request.setIdempotencyKey("key-zero");

        mockMvc.perform(post("/api/v1/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    @DisplayName("Should reject transfer with negative amount")
    void testTransferNegativeAmount() throws Exception {
        TransferRequestDTO request = new TransferRequestDTO();
        request.setFromAccountId("MTS2026-11111111");
        request.setToAccountId("MTS2026-22222222");
        request.setAmount(-50.0);
        request.setIdempotencyKey("key-neg");

        mockMvc.perform(post("/api/v1/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    @DisplayName("Should reject transfer with null sender account")
    void testTransferNullSender() throws Exception {
        TransferRequestDTO request = new TransferRequestDTO();
        request.setFromAccountId(null);
        request.setToAccountId("MTS2026-22222222");
        request.setAmount(100.0);
        request.setIdempotencyKey("key-null-sender");

        mockMvc.perform(post("/api/v1/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    @DisplayName("Should reject transfer with null recipient account")
    void testTransferNullRecipient() throws Exception {
        TransferRequestDTO request = new TransferRequestDTO();
        request.setFromAccountId("MTS2026-11111111");
        request.setToAccountId(null);
        request.setAmount(100.0);
        request.setIdempotencyKey("key-null-recipient");

        mockMvc.perform(post("/api/v1/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Should require authentication for transfer")
    void testTransferRequiresAuth() throws Exception {
        TransferRequestDTO request = new TransferRequestDTO();
        request.setFromAccountId("MTS2026-11111111");
        request.setToAccountId("MTS2026-22222222");
        request.setAmount(100.0);
        request.setIdempotencyKey("key-auth");

        mockMvc.perform(post("/api/v1/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ============ TRANSACTION HISTORY TESTS ============

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    @DisplayName("Should retrieve transaction history for account")
    void testGetTransactionHistory() throws Exception {
        String accountId = "MTS2026-11111111";

        TransactionHistoryDTO history1 = new TransactionHistoryDTO(1L, accountId, "MTS2026-22222222", 100.0, "SUCCESS", null, null);
        TransactionHistoryDTO history2 = new TransactionHistoryDTO(2L, "MTS2026-33333333", accountId, 50.0, "SUCCESS", null, null);

        List<TransactionHistoryDTO> historyList = Arrays.asList(history1, history2);

        when(transferService.getTransactionHistory(accountId)).thenReturn(historyList);

        mockMvc.perform(get("/api/v1/transfers/history/" + accountId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].amount").value(100.0))
                .andExpect(jsonPath("$[1].amount").value(50.0));

        verify(transferService).getTransactionHistory(accountId);
    }

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    @DisplayName("Should return empty history for account with no transactions")
    void testGetTransactionHistoryEmpty() throws Exception {
        String accountId = "MTS2026-11111111";

        when(transferService.getTransactionHistory(accountId)).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/v1/transfers/history/" + accountId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    @DisplayName("Should return multiple transactions in history")
    void testGetTransactionHistoryMultiple() throws Exception {
        String accountId = "MTS2026-11111111";

        List<TransactionHistoryDTO> historyList = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            TransactionHistoryDTO history = new TransactionHistoryDTO(
                    (long) i,
                    accountId,
                    "MTS2026-" + String.format("%08d", 20000000 + i),
                    100.0 * i,
                    "SUCCESS",
                    null,
                    null
            );
            historyList.add(history);
        }

        when(transferService.getTransactionHistory(accountId)).thenReturn(historyList);

        mockMvc.perform(get("/api/v1/transfers/history/" + accountId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5));
    }

    @Test
    @DisplayName("Should require authentication for transaction history")
    void testTransactionHistoryRequiresAuth() throws Exception {
        String accountId = "MTS2026-11111111";

        mockMvc.perform(get("/api/v1/transfers/history/" + accountId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // ============ LARGE TRANSFER TESTS ============

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    @DisplayName("Should handle large transfer amount")
    void testTransferLargeAmount() throws Exception {
        TransferRequestDTO request = new TransferRequestDTO();
        request.setFromAccountId("MTS2026-11111111");
        request.setToAccountId("MTS2026-22222222");
        request.setAmount(999999.99);
        request.setIdempotencyKey("key-large");

        TransferResponseDTO response = new TransferResponseDTO(1L, "SUCCESS", "Transfer completed successfully");

        when(transferService.transfer(any(TransferRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    @DisplayName("Should handle small decimal transfer")
    void testTransferSmallDecimal() throws Exception {
        TransferRequestDTO request = new TransferRequestDTO();
        request.setFromAccountId("MTS2026-11111111");
        request.setToAccountId("MTS2026-22222222");
        request.setAmount(0.01);
        request.setIdempotencyKey("key-small");

        TransferResponseDTO response = new TransferResponseDTO(1L, "SUCCESS", "Transfer completed successfully");

        when(transferService.transfer(any(TransferRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }
}