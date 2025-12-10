package com.Eges411Team.UnifiedPatientManager.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for OtpToken entity.
 * Tests the validity and expiration logic.
 */
class OtpTokenTest {

    private OtpToken otpToken;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        otpToken = new OtpToken();
        otpToken.setOtpId(1L);
        otpToken.setUser(testUser);
        otpToken.setOtpCode("123456");
        otpToken.setCreatedAt(LocalDateTime.now());
        otpToken.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        otpToken.setUsed(false);
        otpToken.setAttemptCount(0);
    }

    // ==================== isValid Tests ====================
    @Test
    void isValid_freshToken_returnsTrue() {
        // A fresh, unused token that hasn't expired should be valid
        assertTrue(otpToken.isValid());
    }

    @Test
    void isValid_usedToken_returnsFalse() {
        // A used token should be invalid even if not expired
        otpToken.setUsed(true);
        assertFalse(otpToken.isValid());
    }

    @Test
    void isValid_expiredToken_returnsFalse() {
        // An expired token should be invalid even if unused
        otpToken.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        assertFalse(otpToken.isValid());
    }

    @Test
    void isValid_tooManyAttempts_returnsFalse() {
        // A token with 3+ attempts should be invalid
        otpToken.setAttemptCount(3);
        assertFalse(otpToken.isValid());
    }

    @Test
    void isValid_usedAndExpired_returnsFalse() {
        // Multiple failure conditions should still return false
        otpToken.setUsed(true);
        otpToken.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        assertFalse(otpToken.isValid());
    }

    @Test
    void isValid_exactlyAtExpirationBoundary_returnsFalse() {
        // Token at exact expiration moment should be invalid
        LocalDateTime now = LocalDateTime.now();
        otpToken.setExpiresAt(now);
        
        // After isAfter check at "now", should be false (expired)
        assertFalse(otpToken.isValid());
    }

    @Test
    void isValid_twoAttemptsButNotExpired_returnsTrue() {
        // Token with 2 attempts (< 3) should still be valid
        otpToken.setAttemptCount(2);
        assertTrue(otpToken.isValid());
    }

    // ==================== isExpired Tests ====================
    @Test
    void isExpired_futureExpiryTime_returnsFalse() {
        // Token that expires in the future should not be expired
        otpToken.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        assertFalse(otpToken.isExpired());
    }

    @Test
    void isExpired_pastExpiryTime_returnsTrue() {
        // Token that expired in the past should be expired
        otpToken.setExpiresAt(LocalDateTime.now().minusMinutes(10));
        assertTrue(otpToken.isExpired());
    }

    @Test
    void isExpired_expiryAtBoundary_returnsTrue() {
        // Token at exact expiration time should be considered expired
        otpToken.setExpiresAt(LocalDateTime.now());
        assertTrue(otpToken.isExpired());
    }

    // ==================== Getters and Setters ====================
    @Test
    void testOtpTokenGettersSetters() {
        OtpToken otp = new OtpToken();
        
        // Test OtpId
        otp.setOtpId(5L);
        assertEquals(5L, otp.getOtpId());
        
        // Test User
        otp.setUser(testUser);
        assertEquals(testUser, otp.getUser());
        
        // Test OTP Code
        otp.setOtpCode("654321");
        assertEquals("654321", otp.getOtpCode());
        
        // Test Created At
        LocalDateTime created = LocalDateTime.now();
        otp.setCreatedAt(created);
        assertEquals(created, otp.getCreatedAt());
        
        // Test Expires At
        LocalDateTime expires = LocalDateTime.now().plusHours(1);
        otp.setExpiresAt(expires);
        assertEquals(expires, otp.getExpiresAt());
        
        // Test Used
        otp.setUsed(true);
        assertTrue(otp.isUsed());
        
        // Test Attempt Count
        otp.setAttemptCount(2);
        assertEquals(2, otp.getAttemptCount());
    }

    @Test
    void testOtpTokenDefaultValues() {
        // Default constructor should set used=false and attemptCount=0
        OtpToken otp = new OtpToken();
        
        assertFalse(otp.isUsed());
        assertEquals(0, otp.getAttemptCount());
    }
}
