package com.bd.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("JwtUtil Tests")
class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    private static final String TEST_USERNAME = "testuser";

    // ============ TOKEN GENERATION TESTS ============

    @Test
    @DisplayName("Should generate valid JWT token")
    void testGenerateValidToken() {
        String token = jwtUtil.generateToken(TEST_USERNAME);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.contains("."), "JWT should contain dots");
    }

    @Test
    @DisplayName("Should generate different tokens for different usernames")
    void testGenerateDifferentTokens() {
        String token1 = jwtUtil.generateToken("user1");
        String token2 = jwtUtil.generateToken("user2");

        assertNotEquals(token1, token2);
    }

    @Test
    @DisplayName("Should generate different tokens on successive calls")
    void testGenerateTokensDifferent() {
        String token1 = jwtUtil.generateToken(TEST_USERNAME);
        String token2 = jwtUtil.generateToken(TEST_USERNAME);

        assertNotEquals(token1, token2, "Tokens should be different due to different issuedAt times");
    }

    // ============ TOKEN EXTRACTION TESTS ============

    @Test
    @DisplayName("Should extract username from valid token")
    void testExtractUsernameSuccess() {
        String token = jwtUtil.generateToken(TEST_USERNAME);

        String extractedUsername = jwtUtil.extractUsername(token);

        assertEquals(TEST_USERNAME, extractedUsername);
    }

    @Test
    @DisplayName("Should return null for invalid token")
    void testExtractUsernameInvalidToken() {
        String invalidToken = "invalid.token.here";

        String extractedUsername = jwtUtil.extractUsername(invalidToken);

        assertNull(extractedUsername);
    }

    @Test
    @DisplayName("Should return null for malformed token")
    void testExtractUsernameMalformedToken() {
        String malformedToken = "not-a-jwt";

        String extractedUsername = jwtUtil.extractUsername(malformedToken);

        assertNull(extractedUsername);
    }

    @Test
    @DisplayName("Should extract username correctly from token with complex characters")
    void testExtractUsernameComplexUser() {
        String complexUsername = "user@example.com";
        String token = jwtUtil.generateToken(complexUsername);

        String extractedUsername = jwtUtil.extractUsername(token);

        assertEquals(complexUsername, extractedUsername);
    }

    // ============ TOKEN VALIDATION TESTS ============

    @Test
    @DisplayName("Should validate correct token")
    void testValidateValidToken() {
        String token = jwtUtil.generateToken(TEST_USERNAME);

        boolean isValid = jwtUtil.validateToken(token);

        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should reject invalid token")
    void testValidateInvalidToken() {
        String invalidToken = "invalid.token.here";

        boolean isValid = jwtUtil.validateToken(invalidToken);

        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should reject malformed token")
    void testValidateMalformedToken() {
        String malformedToken = "not-a-jwt-token";

        boolean isValid = jwtUtil.validateToken(malformedToken);

        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should reject token with wrong signature")
    void testValidateWrongSignature() {
        String token = jwtUtil.generateToken(TEST_USERNAME);
        String tamperedToken = token.substring(0, token.length() - 10) + "0000000000";

        boolean isValid = jwtUtil.validateToken(tamperedToken);

        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should reject empty token")
    void testValidateEmptyToken() {
        boolean isValid = jwtUtil.validateToken("");

        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should reject null token")
    void testValidateNullToken() {
        boolean isValid = jwtUtil.validateToken(null);

        assertFalse(isValid);
    }

    // ============ ROUND TRIP TESTS ============

    @Test
    @DisplayName("Should perform complete generate-validate-extract cycle")
    void testCompleteTokenCycle() {
        String originalUsername = "cycleuser";

        // Generate token
        String token = jwtUtil.generateToken(originalUsername);
        assertNotNull(token);

        // Validate token
        assertTrue(jwtUtil.validateToken(token));

        // Extract username
        String extractedUsername = jwtUtil.extractUsername(token);
        assertEquals(originalUsername, extractedUsername);
    }

    @Test
    @DisplayName("Should handle multiple usernames in sequence")
    void testMultipleUsersSequence() {
        String[] usernames = {"alice", "bob", "charlie", "diana", "eve"};

        for (String username : usernames) {
            String token = jwtUtil.generateToken(username);
            assertTrue(jwtUtil.validateToken(token));
            assertEquals(username, jwtUtil.extractUsername(token));
        }
    }

    // ============ EDGE CASES ============

    @Test
    @DisplayName("Should handle username with special characters")
    void testSpecialCharacterUsername() {
        String specialUsername = "user+test_123-456@domain.co";
        String token = jwtUtil.generateToken(specialUsername);

        assertTrue(jwtUtil.validateToken(token));
        assertEquals(specialUsername, jwtUtil.extractUsername(token));
    }

    @Test
    @DisplayName("Should handle very long username")
    void testLongUsername() {
        String longUsername = "a".repeat(255);
        String token = jwtUtil.generateToken(longUsername);

        assertTrue(jwtUtil.validateToken(token));
        assertEquals(longUsername, jwtUtil.extractUsername(token));
    }

    @Test
    @DisplayName("Token should contain proper JWT structure")
    void testTokenStructure() {
        String token = jwtUtil.generateToken(TEST_USERNAME);

        String[] parts = token.split("\\.");
        assertEquals(3, parts.length, "JWT should have 3 parts separated by dots");
    }

    @Test
    @DisplayName("Should not validate empty or whitespace tokens")
    void testValidateWhitespaceToken() {
        assertFalse(jwtUtil.validateToken("   "));
        assertFalse(jwtUtil.validateToken("\t"));
        assertFalse(jwtUtil.validateToken("\n"));
    }
}
