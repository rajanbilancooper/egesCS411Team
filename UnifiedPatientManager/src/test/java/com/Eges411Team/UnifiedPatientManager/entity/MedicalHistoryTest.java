package com.Eges411Team.UnifiedPatientManager.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MedicalHistory entity.
 * Tests null-safe getter/setter for prescribeMedication field.
 */
class MedicalHistoryTest {

    private MedicalHistory medicalHistory;

    @BeforeEach
    void setUp() {
        medicalHistory = new MedicalHistory();
        medicalHistory.setId(1L);
        medicalHistory.setPatientId(100L);
        medicalHistory.setDoctorId(200L);
        medicalHistory.setDiagnosis("Test Diagnosis");
        medicalHistory.setFrequency("Daily");
        medicalHistory.setStartDate(new Date());
        medicalHistory.setEndDate(new Date());
    }

    @Test
    void prescribeMedication_setNull_getsAsfalse() {
        // When setting to null, the setter should convert to FALSE
        medicalHistory.setPrescribeMedication(null);
        
        // Getter should return false (not null)
        assertFalse(medicalHistory.getPrescribeMedication());
        assertNotNull(medicalHistory.getPrescribeMedication());
    }

    @Test
    void prescribeMedication_setTrue_getsAsTrue() {
        // When setting to true, should remain true
        medicalHistory.setPrescribeMedication(true);
        
        assertTrue(medicalHistory.getPrescribeMedication());
    }

    @Test
    void prescribeMedication_setFalse_getsAsFalse() {
        // When setting to false, should remain false
        medicalHistory.setPrescribeMedication(false);
        
        assertFalse(medicalHistory.getPrescribeMedication());
    }

    @Test
    void prescribeMedication_nullField_getterReturnsFalse() {
        // If the internal field is null (e.g., from legacy data),
        // getter should return FALSE (not null)
        // This tests the null-safe getter behavior
        medicalHistory.setPrescribeMedication(null);
        
        Boolean result = medicalHistory.getPrescribeMedication();
        
        assertNotNull(result);
        assertEquals(false, result);
    }

    @Test
    void testMedicalHistoryGettersSetters() {
        // Test all other fields
        medicalHistory.setId(5L);
        assertEquals(5L, medicalHistory.getId());

        medicalHistory.setPatientId(150L);
        assertEquals(150L, medicalHistory.getPatientId());

        medicalHistory.setDoctorId(250L);
        assertEquals(250L, medicalHistory.getDoctorId());

        medicalHistory.setDiagnosis("Updated Diagnosis");
        assertEquals("Updated Diagnosis", medicalHistory.getDiagnosis());

        medicalHistory.setFrequency("Weekly");
        assertEquals("Weekly", medicalHistory.getFrequency());

        Date newStartDate = new Date(System.currentTimeMillis() - 86400000);
        medicalHistory.setStartDate(newStartDate);
        assertEquals(newStartDate, medicalHistory.getStartDate());

        Date newEndDate = new Date(System.currentTimeMillis() + 86400000);
        medicalHistory.setEndDate(newEndDate);
        assertEquals(newEndDate, medicalHistory.getEndDate());
    }
}
