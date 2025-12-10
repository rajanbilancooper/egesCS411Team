package com.Eges411Team.UnifiedPatientManager.serviceUnitTests.allergyTests;

import com.Eges411Team.UnifiedPatientManager.entity.Allergy;
import com.Eges411Team.UnifiedPatientManager.repositories.AllergyRepository;
import com.Eges411Team.UnifiedPatientManager.services.AllergyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test Case: Get/Retrieve Allergies
 * Tests AllergyService.getAllergiesByPatientId() and refreshAllergies() methods
 */
@ExtendWith(MockitoExtension.class)
class getAllergiesTest {

    @Mock
    private AllergyRepository allergyRepository;

    @InjectMocks
    private AllergyService allergyService;

    @Test
    void getAllergiesByPatientId_patientHasNoAllergies_returnsEmptyList() {
        // Arrange - Patient with no allergies
        Long patientId = 123L;

        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());

        // Act - Get allergies for patient
        List<Allergy> result = allergyService.getAllergiesByPatientId(patientId);

        // Assert - Verify empty list is returned
        assertNotNull(result, "Result should not be null");
        assertTrue(result.isEmpty(), "Result should be empty list");
        assertEquals(0, result.size(), "Size should be 0");

        verify(allergyRepository, times(1)).findAllByPatientId(patientId);
    }

    @Test
    void getAllergiesByPatientId_patientHasOneAllergy_returnsListWithOneElement() {
        // Arrange - Patient with one allergy
        Long patientId = 123L;

        Allergy allergy1 = new Allergy();
        allergy1.setId(1L);
        allergy1.setPatientId(patientId);
        allergy1.setSubstance("Peanuts");
        allergy1.setSeverity("HIGH");
        allergy1.setReaction("Anaphylaxis");

        List<Allergy> allergies = new ArrayList<>();
        allergies.add(allergy1);

        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(allergies);

        // Act
        List<Allergy> result = allergyService.getAllergiesByPatientId(patientId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size(), "Should have 1 allergy");
        assertEquals("Peanuts", result.get(0).getSubstance());
        assertEquals(patientId, result.get(0).getPatientId());

        verify(allergyRepository, times(1)).findAllByPatientId(patientId);
    }

    @Test
    void getAllergiesByPatientId_patientHasMultipleAllergies_returnsAllAllergies() {
        // Arrange - Patient with multiple allergies
        Long patientId = 123L;

        Allergy allergy1 = new Allergy();
        allergy1.setId(1L);
        allergy1.setPatientId(patientId);
        allergy1.setSubstance("Peanuts");
        allergy1.setSeverity("HIGH");
        allergy1.setReaction("Anaphylaxis");

        Allergy allergy2 = new Allergy();
        allergy2.setId(2L);
        allergy2.setPatientId(patientId);
        allergy2.setSubstance("Shellfish");
        allergy2.setSeverity("MEDIUM");
        allergy2.setReaction("Hives");

        Allergy allergy3 = new Allergy();
        allergy3.setId(3L);
        allergy3.setPatientId(patientId);
        allergy3.setSubstance("Latex");
        allergy3.setSeverity("LOW");
        allergy3.setReaction("Rash");

        List<Allergy> allergies = Arrays.asList(allergy1, allergy2, allergy3);

        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(allergies);

        // Act
        List<Allergy> result = allergyService.getAllergiesByPatientId(patientId);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size(), "Should have 3 allergies");
        assertEquals("Peanuts", result.get(0).getSubstance());
        assertEquals("Shellfish", result.get(1).getSubstance());
        assertEquals("Latex", result.get(2).getSubstance());

        verify(allergyRepository, times(1)).findAllByPatientId(patientId);
    }

    @Test
    void getAllergiesByPatientId_differentPatients_returnsOnlyPatientAllergies() {
        // Arrange - Multiple patients, verify correct patient's allergies returned
        Long patientId1 = 123L;
        Long patientId2 = 456L;

        Allergy allergy1 = new Allergy();
        allergy1.setId(1L);
        allergy1.setPatientId(patientId1);
        allergy1.setSubstance("Peanuts");

        when(allergyRepository.findAllByPatientId(patientId1)).thenReturn(Arrays.asList(allergy1));
        when(allergyRepository.findAllByPatientId(patientId2)).thenReturn(new ArrayList<>());

        // Act
        List<Allergy> result1 = allergyService.getAllergiesByPatientId(patientId1);
        List<Allergy> result2 = allergyService.getAllergiesByPatientId(patientId2);

        // Assert
        assertEquals(1, result1.size(), "Patient 1 should have 1 allergy");
        assertEquals(0, result2.size(), "Patient 2 should have 0 allergies");

        verify(allergyRepository, times(1)).findAllByPatientId(patientId1);
        verify(allergyRepository, times(1)).findAllByPatientId(patientId2);
    }

    @Test
    void refreshAllergies_patientHasNoAllergies_returnsEmptyList() {
        // Arrange - Test refresh method (same logic as get)
        Long patientId = 789L;

        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());

        // Act
        List<Allergy> result = allergyService.refreshAllergies(patientId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(allergyRepository, times(1)).findAllByPatientId(patientId);
    }

    @Test
    void refreshAllergies_patientHasAllergies_returnsFreshData() {
        // Arrange - Test refresh returns fresh data
        Long patientId = 789L;

        Allergy allergy1 = new Allergy();
        allergy1.setId(1L);
        allergy1.setPatientId(patientId);
        allergy1.setSubstance("Penicillin");

        List<Allergy> allergies = new ArrayList<>();
        allergies.add(allergy1);

        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(allergies);

        // Act
        List<Allergy> result = allergyService.refreshAllergies(patientId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Penicillin", result.get(0).getSubstance());

        verify(allergyRepository, times(1)).findAllByPatientId(patientId);
    }
}
