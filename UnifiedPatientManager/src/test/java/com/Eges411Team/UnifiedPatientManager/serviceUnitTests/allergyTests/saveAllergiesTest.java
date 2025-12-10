package com.Eges411Team.UnifiedPatientManager.serviceUnitTests.allergyTests;

import com.Eges411Team.UnifiedPatientManager.entity.Allergy;
import com.Eges411Team.UnifiedPatientManager.repositories.AllergyRepository;
import com.Eges411Team.UnifiedPatientManager.services.AllergyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test Case: Save/Replace All Allergies
 * Tests AllergyService.saveAllergies() method which replaces all allergies for a patient
 * Requirement: 3.1.1 - The system shall maintain complete medical histories
 */
@ExtendWith(MockitoExtension.class)
class saveAllergiesTest {

    @Mock
    private AllergyRepository allergyRepository;

    @InjectMocks
    private AllergyService allergyService;

    @Test
    void saveAllergies_replaceExistingAllergiesWithNewList_successfullyReplaces() {
        // Arrange - Patient has existing allergies that should be deleted
        Long patientId = 123L;

        Allergy existingAllergy1 = new Allergy();
        existingAllergy1.setId(1L);
        existingAllergy1.setPatientId(patientId);
        existingAllergy1.setSubstance("Peanuts");

        Allergy existingAllergy2 = new Allergy();
        existingAllergy2.setId(2L);
        existingAllergy2.setPatientId(patientId);
        existingAllergy2.setSubstance("Shellfish");

        List<Allergy> existingAllergies = Arrays.asList(existingAllergy1, existingAllergy2);

        Allergy newAllergy1 = new Allergy();
        newAllergy1.setSubstance("Milk");
        newAllergy1.setSeverity("MEDIUM");
        newAllergy1.setReaction("Digestive issues");

        Allergy newAllergy2 = new Allergy();
        newAllergy2.setSubstance("Soy");
        newAllergy2.setSeverity("LOW");
        newAllergy2.setReaction("Mild itching");

        List<Allergy> newAllergies = Arrays.asList(newAllergy1, newAllergy2);

        List<Allergy> savedAllergies = new ArrayList<>();
        Allergy saved1 = new Allergy();
        saved1.setId(3L);
        saved1.setPatientId(patientId);
        saved1.setSubstance("Milk");
        saved1.setSeverity("MEDIUM");
        saved1.setReaction("Digestive issues");

        Allergy saved2 = new Allergy();
        saved2.setId(4L);
        saved2.setPatientId(patientId);
        saved2.setSubstance("Soy");
        saved2.setSeverity("LOW");
        saved2.setReaction("Mild itching");

        savedAllergies.add(saved1);
        savedAllergies.add(saved2);

        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(existingAllergies);
        when(allergyRepository.saveAll(anyList())).thenReturn(savedAllergies);

        // Act
        List<Allergy> result = allergyService.saveAllergies(patientId, newAllergies);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals(2, result.size(), "Should have 2 allergies");
        assertEquals("Milk", result.get(0).getSubstance(), "First allergy substance should be Milk");
        assertEquals("Soy", result.get(1).getSubstance(), "Second allergy substance should be Soy");

        // Verify deletion of existing allergies
        verify(allergyRepository, times(1)).findAllByPatientId(patientId);
        verify(allergyRepository, times(1)).deleteAll(existingAllergies);
        verify(allergyRepository, times(1)).saveAll(anyList());

        // Verify patient IDs were set on new allergies
        ArgumentCaptor<List<Allergy>> captor = ArgumentCaptor.forClass(List.class);
        verify(allergyRepository).saveAll(captor.capture());
        List<Allergy> savedList = captor.getValue();
        for (Allergy allergy : savedList) {
            assertEquals(patientId, allergy.getPatientId(), "All allergies should have patient ID set");
        }
    }

    @Test
    void saveAllergies_patientHasNoExistingAllergies_savesNewAllergiesWithEmptyList() {
        // Arrange - Patient with no existing allergies
        Long patientId = 456L;

        Allergy newAllergy = new Allergy();
        newAllergy.setSubstance("Penicillin");
        newAllergy.setSeverity("HIGH");
        newAllergy.setReaction("Anaphylaxis");

        List<Allergy> newAllergies = Arrays.asList(newAllergy);
        List<Allergy> existingAllergies = new ArrayList<>();

        Allergy savedAllergy = new Allergy();
        savedAllergy.setId(10L);
        savedAllergy.setPatientId(patientId);
        savedAllergy.setSubstance("Penicillin");
        savedAllergy.setSeverity("HIGH");
        savedAllergy.setReaction("Anaphylaxis");

        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(existingAllergies);
        when(allergyRepository.saveAll(anyList())).thenReturn(Arrays.asList(savedAllergy));

        // Act
        List<Allergy> result = allergyService.saveAllergies(patientId, newAllergies);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Penicillin", result.get(0).getSubstance());

        verify(allergyRepository, times(1)).findAllByPatientId(patientId);
        verify(allergyRepository, times(1)).deleteAll(existingAllergies);
        verify(allergyRepository, times(1)).saveAll(anyList());
    }

    @Test
    void saveAllergies_saveEmptyListRemovesAllAllergies() {
        // Arrange - Replace all allergies with empty list (delete all)
        Long patientId = 789L;

        Allergy existingAllergy1 = new Allergy();
        existingAllergy1.setId(1L);
        existingAllergy1.setPatientId(patientId);
        existingAllergy1.setSubstance("Latex");

        Allergy existingAllergy2 = new Allergy();
        existingAllergy2.setId(2L);
        existingAllergy2.setPatientId(patientId);
        existingAllergy2.setSubstance("Iodine");

        List<Allergy> existingAllergies = Arrays.asList(existingAllergy1, existingAllergy2);
        List<Allergy> emptyList = new ArrayList<>();

        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(existingAllergies);
        when(allergyRepository.saveAll(anyList())).thenReturn(new ArrayList<>());

        // Act
        List<Allergy> result = allergyService.saveAllergies(patientId, emptyList);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty(), "Result should be empty");

        verify(allergyRepository, times(1)).findAllByPatientId(patientId);
        verify(allergyRepository, times(1)).deleteAll(existingAllergies);
        verify(allergyRepository, times(1)).saveAll(emptyList);
    }

    @Test
    void saveAllergies_setsPatientIdOnAllNewAllergies() {
        // Arrange - Verify patient ID is set on all new allergies
        Long patientId = 999L;

        Allergy allergy1 = new Allergy();
        allergy1.setSubstance("Aspirin");
        allergy1.setPatientId(null); // Start with null

        Allergy allergy2 = new Allergy();
        allergy2.setSubstance("Ibuprofen");
        allergy2.setPatientId(null); // Start with null

        List<Allergy> newAllergies = Arrays.asList(allergy1, allergy2);

        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(allergyRepository.saveAll(anyList())).thenAnswer(inv -> {
            List<Allergy> list = inv.getArgument(0);
            // Simulate ID assignment after save
            list.get(0).setId(1L);
            list.get(1).setId(2L);
            return list;
        });

        // Act
        List<Allergy> result = allergyService.saveAllergies(patientId, newAllergies);

        // Assert
        assertEquals(patientId, newAllergies.get(0).getPatientId(), "First allergy should have patient ID");
        assertEquals(patientId, newAllergies.get(1).getPatientId(), "Second allergy should have patient ID");

        verify(allergyRepository, times(1)).saveAll(anyList());
    }

    @Test
    void saveAllergies_loopProcessesAllAllergiesInList() {
        // Arrange - Test that loop processes all items in list (branch coverage)
        Long patientId = 111L;

        List<Allergy> newAllergies = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Allergy allergy = new Allergy();
            allergy.setSubstance("Allergy" + i);
            newAllergies.add(allergy);
        }

        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(allergyRepository.saveAll(anyList())).thenAnswer(inv -> {
            List<Allergy> list = inv.getArgument(0);
            for (int i = 0; i < list.size(); i++) {
                list.get(i).setId((long) (i + 1));
            }
            return list;
        });

        // Act
        List<Allergy> result = allergyService.saveAllergies(patientId, newAllergies);

        // Assert
        assertEquals(5, result.size(), "Should have 5 allergies");

        // Verify all allergies have patient ID set via loop
        for (Allergy allergy : newAllergies) {
            assertEquals(patientId, allergy.getPatientId(), "All allergies should have patient ID set");
        }

        verify(allergyRepository, times(1)).findAllByPatientId(patientId);
        verify(allergyRepository, times(1)).deleteAll(anyList());
        verify(allergyRepository, times(1)).saveAll(anyList());
    }
}
