package com.bd.controller;

import com.bd.dto.LoginRequest;
import com.bd.dto.LoginResponse;
import com.bd.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @BeforeEach
    void setUp() {
        reset(authService);
    }

    // ============ REGISTER ENDPOINT TESTS ============

    @Test
    @DisplayName("Should register user successfully")
    void testRegisterSuccess() throws Exception {
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo("MTS2026-12345678", "John Doe");
        LoginResponse response = new LoginResponse(true, "jwt-token-123", userInfo, "USER");

        when(authService.register("johndoe", "password123", "John Doe")).thenReturn(Optional.of(response));

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", "johndoe")
                .param("password", "password123")
                .param("holderName", "John Doe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(true))
                .andExpect(jsonPath("$.token").value("jwt-token-123"))
                .andExpect(jsonPath("$.user.id").value("MTS2026-12345678"))
                .andExpect(jsonPath("$.user.name").value("John Doe"));

        verify(authService).register("johndoe", "password123", "John Doe");
    }

    @Test
    @DisplayName("Should return error when username already exists")
    void testRegisterUserExists() throws Exception {
        when(authService.register("johndoe", "password123", "John Doe"))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", "johndoe")
                .param("password", "password123")
                .param("holderName", "John Doe"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Should return error on register with null username")
    void testRegisterNullUsername() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("password", "password123")
                .param("holderName", "John Doe"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Should return error on register with null password")
    void testRegisterNullPassword() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", "johndoe")
                .param("holderName", "John Doe"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Should return error on register with null holder name")
    void testRegisterNullHolderName() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", "johndoe")
                .param("password", "password123"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Should return error on register with empty username")
    void testRegisterEmptyUsername() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", "")
                .param("password", "password123")
                .param("holderName", "John Doe"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Should return error on register with empty password")
    void testRegisterEmptyPassword() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", "johndoe")
                .param("password", "")
                .param("holderName", "John Doe"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Should register user with special characters in name")
    void testRegisterSpecialCharactersInName() throws Exception {
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo("MTS2026-98765432", "José María O'Connor");
        LoginResponse response = new LoginResponse(true, "jwt-token-special", userInfo, "USER");

        when(authService.register("user123", "password123", "José María O'Connor")).thenReturn(Optional.of(response));

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", "user123")
                .param("password", "password123")
                .param("holderName", "José María O'Connor"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.name").value("José María O'Connor"));
    }

    // ============ LOGIN ENDPOINT TESTS ============

    @Test
    @DisplayName("Should login user successfully with valid credentials")
    void testLoginSuccess() throws Exception {
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo("MTS2026-87654321", "Jane Doe");
        LoginResponse response = new LoginResponse(true, "jwt-token-456", userInfo, "USER");

        LoginRequest request = new LoginRequest();
        request.setUsername("janedoe");
        request.setPassword("password456");

        when(authService.login(any(LoginRequest.class))).thenReturn(Optional.of(response));

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(true))
                .andExpect(jsonPath("$.token").value("jwt-token-456"))
                .andExpect(jsonPath("$.user.id").value("MTS2026-87654321"))
                .andExpect(jsonPath("$.user.name").value("Jane Doe"));
    }

    @Test
    @DisplayName("Should fail login with invalid username")
    void testLoginInvalidUsername() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("nonexistentuser");
        request.setPassword("password456");

        when(authService.login(any(LoginRequest.class)))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Should fail login with incorrect password")
    void testLoginInvalidPassword() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("janedoe");
        request.setPassword("wrongpassword");

        when(authService.login(any(LoginRequest.class)))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Should fail login with null username")
    void testLoginNullUsername() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername(null);
        request.setPassword("password456");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Should fail login with null password")
    void testLoginNullPassword() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("janedoe");
        request.setPassword(null);

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Should fail login with empty username")
    void testLoginEmptyUsername() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("");
        request.setPassword("password456");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Should fail login with empty password")
    void testLoginEmptyPassword() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("janedoe");
        request.setPassword("");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Should include JWT token in login response")
    void testLoginTokenPresent() throws Exception {
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo("MTS2026-11111111", "User");
        LoginResponse response = new LoginResponse(true, "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", userInfo, "USER");

        LoginRequest request = new LoginRequest();
        request.setUsername("user");
        request.setPassword("password");

        when(authService.login(any(LoginRequest.class))).thenReturn(Optional.of(response));

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assert(content.contains("token"));
        assert(!content.contains("\"token\":\"\""));
    }

    @Test
    @DisplayName("Should login with special characters in username")
    void testLoginSpecialCharacters() throws Exception {
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo("MTS2026-55555555", "Special User");
        LoginResponse response = new LoginResponse(true, "jwt-token-special", userInfo, "USER");

        LoginRequest request = new LoginRequest();
        request.setUsername("user.name@123");
        request.setPassword("pass@word123");

        when(authService.login(any(LoginRequest.class))).thenReturn(Optional.of(response));

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(true));
    }

    // ============ USERNAME AND HOLDER NAME VALIDATION TESTS ============

    @Test
    @DisplayName("Should reject registration with short username")
    void testRegisterRejectShortUsername() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", "ab")
                .param("password", "Pass@word123")
                .param("holderName", "John Doe"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Should reject registration with invalid username characters")
    void testRegisterRejectInvalidUsernameChars() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", "user@domain")
                .param("password", "Pass@word123")
                .param("holderName", "John Doe"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Should accept registration with valid username format")
    void testRegisterAcceptValidUsernameFormat() throws Exception {
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo("MTS2026-12345678", "John Doe");
        LoginResponse response = new LoginResponse(true, "jwt-token-123", userInfo, "USER");

        when(authService.register("user.name_123", "Pass@word123", "John Doe")).thenReturn(Optional.of(response));

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", "user.name_123")
                .param("password", "Pass@word123")
                .param("holderName", "John Doe"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should reject registration with short holder name")
    void testRegisterRejectShortHolderName() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", "validuser")
                .param("password", "Pass@word123")
                .param("holderName", "A"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Should reject registration with invalid holder name characters")
    void testRegisterRejectInvalidHolderNameChars() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", "validuser")
                .param("password", "Pass@word123")
                .param("holderName", "John123Doe"))
                .andExpect(status().is4xxClientError());
    }
}