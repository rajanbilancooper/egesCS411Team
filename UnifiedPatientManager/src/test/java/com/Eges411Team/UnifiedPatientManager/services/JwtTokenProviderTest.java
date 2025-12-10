package com.Eges411Team.UnifiedPatientManager.services;

import com.Eges411Team.UnifiedPatientManager.entity.Role;
import com.Eges411Team.UnifiedPatientManager.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtTokenProvider.
 * Tests token generation, validation, and extraction of claims.
 */
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private User testUser;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setRole(Role.PATIENT);
    }

    // ==================== generateToken Tests ====================
    @Test
    void generateToken_validUser_returnsToken() {
        // Should generate a non-null, non-empty token
        String token = jwtTokenProvider.generateToken(testUser);
        
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.contains("."));  // JWT format has dots
    }

    @Test
    void generateToken_multipleUsers_generatesDifferentTokens() {
        // Different users should generate different tokens (based on username)
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("anotheruser");
        user2.setRole(Role.DOCTOR);
        
        String token1 = jwtTokenProvider.generateToken(testUser);
        String token2 = jwtTokenProvider.generateToken(user2);
        
        assertNotEquals(token1, token2);
    }

    // ==================== validateToken Tests ====================
    @Test
    void validateToken_validToken_returnsTrue() {
        // A token we just generated should be valid
        String token = jwtTokenProvider.generateToken(testUser);
        
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    void validateToken_invalidToken_returnsFalse() {
        // A malformed or corrupted token should be invalid
        String invalidToken = "invalid.jwt.token";
        
        assertFalse(jwtTokenProvider.validateToken(invalidToken));
    }

    @Test
    void validateToken_emptyToken_returnsFalse() {
        // Empty token should be invalid
        assertFalse(jwtTokenProvider.validateToken(""));
    }

    @Test
    void validateToken_nullToken_returnsFalse() {
        // Null token should be invalid (catch IllegalArgumentException)
        assertFalse(jwtTokenProvider.validateToken(null));
    }

    // ==================== getUsernameFromToken Tests ====================
    @Test
    void getUsernameFromToken_validToken_returnsUsername() {
        // Should extract the username from a valid token
        String token = jwtTokenProvider.generateToken(testUser);
        
        String username = jwtTokenProvider.getUsernameFromToken(token);
        
        assertEquals("testuser", username);
    }

    @Test
    void getUsernameFromToken_differentUser_returnsCorrectUsername() {
        // Should extract the correct username for different users
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("anotheruser");
        user2.setRole(Role.DOCTOR);
        
        String token = jwtTokenProvider.generateToken(user2);
        
        String username = jwtTokenProvider.getUsernameFromToken(token);
        
        assertEquals("anotheruser", username);
    }

    // ==================== getUserIdFromToken Tests ====================
    @Test
    void getUserIdFromToken_validToken_returnsUserId() {
        // Should extract the user ID from a valid token
        String token = jwtTokenProvider.generateToken(testUser);
        
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        
        assertEquals(1L, userId);
    }

    @Test
    void getUserIdFromToken_differentUserId_returnsCorrectId() {
        // Should extract the correct ID for different users
        User user2 = new User();
        user2.setId(999L);
        user2.setUsername("anotheruser");
        user2.setRole(Role.DOCTOR);
        
        String token = jwtTokenProvider.generateToken(user2);
        
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        
        assertEquals(999L, userId);
    }

    // ==================== getExpirationTime Tests ====================
    @Test
    void getExpirationTime_returnsExpectedTTL() {
        // Should return 24 hours in seconds (86400)
        Long expirationTime = jwtTokenProvider.getExpirationTime();
        
        assertNotNull(expirationTime);
        assertEquals(86400L, expirationTime);
    }

    @Test
    void getExpirationTime_consistency_sameValueEachCall() {
        // Multiple calls should return the same value
        Long exp1 = jwtTokenProvider.getExpirationTime();
        Long exp2 = jwtTokenProvider.getExpirationTime();
        
        assertEquals(exp1, exp2);
    }

    @Test
    void validateToken_malformedTokenVariations_returnsFalse() {
        // Test various malformed token formats
        assertFalse(jwtTokenProvider.validateToken(""));
        assertFalse(jwtTokenProvider.validateToken("justonepart"));
        assertFalse(jwtTokenProvider.validateToken("part1.part2.part3.part4"));
        assertFalse(jwtTokenProvider.validateToken("!!!invalid!!!"));
    }
}
