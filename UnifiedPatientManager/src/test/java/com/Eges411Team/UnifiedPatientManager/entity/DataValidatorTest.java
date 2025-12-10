package com.Eges411Team.UnifiedPatientManager.entity;

import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DataValidator utility class.
 * Tests all validation methods for valid and invalid inputs.
 */
class DataValidatorTest {

    // ==================== isValidDate Tests ====================
    @Test
    void isValidDate_pastDate_returnsTrue() {
        // A date in the past should be valid
        Date pastDate = new Date(System.currentTimeMillis() - 86400000); // 24 hours ago
        
        assertTrue(DataValidator.isValidDate(pastDate));
    }

    @Test
    void isValidDate_futureDate_returnsFalse() {
        // A date in the future should be invalid
        Date futureDate = new Date(System.currentTimeMillis() + 86400000); // 24 hours from now
        
        assertFalse(DataValidator.isValidDate(futureDate));
    }

    @Test
    void isValidDate_currentTime_returnsFalse() {
        // Current time is not strictly before now, so should be false
        Date currentDate = new Date();
        
        assertFalse(DataValidator.isValidDate(currentDate));
    }

    // ==================== isValidString Tests ====================
    @Test
    void isValidString_nonEmptyString_returnsTrue() {
        // A non-empty, non-null string should be valid
        assertTrue(DataValidator.isValidString("valid string"));
    }

    @Test
    void isValidString_nullString_returnsFalse() {
        // A null string should be invalid
        assertFalse(DataValidator.isValidString(null));
    }

    @Test
    void isValidString_emptyString_returnsFalse() {
        // An empty string should be invalid
        assertFalse(DataValidator.isValidString(""));
    }

    @Test
    void isValidString_whitespaceOnlyString_returnsFalse() {
        // A string with only whitespace should be invalid (after trim)
        assertFalse(DataValidator.isValidString("   "));
    }

    @Test
    void isValidString_stringWithWhitespace_returnsTrue() {
        // A string with content and whitespace should be valid
        assertTrue(DataValidator.isValidString("  valid string  "));
    }

    @Test
    void isValidString_singleCharacter_returnsTrue() {
        // A single character string should be valid
        assertTrue(DataValidator.isValidString("a"));
    }
}
