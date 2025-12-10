package com.Eges411Team.UnifiedPatientManager.serviceUnitTests.allergyTests;

import com.Eges411Team.UnifiedPatientManager.entity.Allergy;
import com.Eges411Team.UnifiedPatientManager.repositories.AllergyRepository;
import com.Eges411Team.UnifiedPatientManager.services.AllergyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test Case 24: Create an allergy
 * Use Case: Retrieve Patient Record for update changes
 * Requirement: 3.1.1 - The system shall maintain complete medical histories, including past 
 *              diagnoses, treatments, prescriptions, allergies, and vaccinations and 
 *              constantly keep it updated
 * Technique: Equivalence Partitioning - Testing the valid input class for allergy creation
 */
@ExtendWith(MockitoExtension.class)
class createAllergyTest {

    @Mock
    private AllergyRepository allergyRepository;

    @InjectMocks
    private AllergyService allergyService;

    @Test
    void addSingleAllergy_validAllergyData_successfullyCreatesAllergy() {
        // Arrange - Create valid allergy data
        Long patientId = 123L;
        
        Allergy newAllergy = new Allergy();
        newAllergy.setSubstance("Peanuts");
        newAllergy.setSeverity("HIGH");
        newAllergy.setReaction("Anaphylaxis");

        // Mock: No existing allergies for this patient (empty list)
        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());

        // Mock: Return saved allergy with ID
        Allergy savedAllergy = new Allergy();
        savedAllergy.setId(1L);
        savedAllergy.setPatientId(patientId);
        savedAllergy.setSubstance("Peanuts");
        savedAllergy.setSeverity("HIGH");
        savedAllergy.setReaction("Anaphylaxis");

        when(allergyRepository.save(any(Allergy.class))).thenReturn(savedAllergy);

        // Act - Doctor saves the allergy
        Allergy result = allergyService.addSingleAllergy(patientId, newAllergy);

        // Assert - Verify allergy was created successfully
        assertNotNull(result, "Created allergy should not be null");
        assertNotNull(result.getId(), "Allergy should have an ID");
        assertEquals(patientId, result.getPatientId(), "Patient ID should match");
        assertEquals("Peanuts", result.getSubstance(), "Allergy substance should match");
        assertEquals("HIGH", result.getSeverity(), "Severity should match");
        assertEquals("Anaphylaxis", result.getReaction(), "Reaction should match");

        // Verify repository methods were called
        verify(allergyRepository, times(1)).findAllByPatientId(patientId);
        verify(allergyRepository, times(1)).save(any(Allergy.class));
    }

    @Test
    void addSingleAllergy_lowSeverityAllergy_successfullyCreatesAllergy() {
        // Arrange - Test with LOW severity (equivalence partitioning - different severity class)
        Long patientId = 123L;
        
        Allergy newAllergy = new Allergy();
        newAllergy.setSubstance("Dust");
        newAllergy.setSeverity("LOW");
        newAllergy.setReaction("Sneezing");

        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());

        Allergy savedAllergy = new Allergy();
        savedAllergy.setId(2L);
        savedAllergy.setPatientId(patientId);
        savedAllergy.setSubstance("Dust");
        savedAllergy.setSeverity("LOW");
        savedAllergy.setReaction("Sneezing");

        when(allergyRepository.save(any(Allergy.class))).thenReturn(savedAllergy);

        // Act
        Allergy result = allergyService.addSingleAllergy(patientId, newAllergy);

        // Assert
        assertNotNull(result);
        assertEquals("Dust", result.getSubstance());
        assertEquals("LOW", result.getSeverity());
        assertEquals("Sneezing", result.getReaction());

        verify(allergyRepository, times(1)).save(any(Allergy.class));
    }

    @Test
    void addSingleAllergy_mediumSeverityAllergy_successfullyCreatesAllergy() {
        // Arrange - Test with MEDIUM severity (equivalence partitioning - another severity class)
        Long patientId = 123L;
        
        Allergy newAllergy = new Allergy();
        newAllergy.setSubstance("Shellfish");
        newAllergy.setSeverity("MEDIUM");
        newAllergy.setReaction("Hives");

        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());

        Allergy savedAllergy = new Allergy();
        savedAllergy.setId(3L);
        savedAllergy.setPatientId(patientId);
        savedAllergy.setSubstance("Shellfish");
        savedAllergy.setSeverity("MEDIUM");
        savedAllergy.setReaction("Hives");

        when(allergyRepository.save(any(Allergy.class))).thenReturn(savedAllergy);

        // Act
        Allergy result = allergyService.addSingleAllergy(patientId, newAllergy);

        // Assert
        assertNotNull(result);
        assertEquals("Shellfish", result.getSubstance());
        assertEquals("MEDIUM", result.getSeverity());
        assertEquals("Hives", result.getReaction());

        verify(allergyRepository, times(1)).save(any(Allergy.class));
    }

    @Test
    void addSingleAllergy_multipleAllergiesForPatient_addsToExistingList() {
        // Arrange - Patient already has existing allergies
        Long patientId = 123L;
        
        // Existing allergy
        Allergy existingAllergy = new Allergy();
        existingAllergy.setId(1L);
        existingAllergy.setPatientId(patientId);
        existingAllergy.setSubstance("Peanuts");
        existingAllergy.setSeverity("HIGH");
        existingAllergy.setReaction("Anaphylaxis");

        List<Allergy> existingAllergies = new ArrayList<>();
        existingAllergies.add(existingAllergy);

        // New allergy to add
        Allergy newAllergy = new Allergy();
        newAllergy.setSubstance("Penicillin");
        newAllergy.setSeverity("HIGH");
        newAllergy.setReaction("Rash");

        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(existingAllergies);

        Allergy savedAllergy = new Allergy();
        savedAllergy.setId(2L);
        savedAllergy.setPatientId(patientId);
        savedAllergy.setSubstance("Penicillin");
        savedAllergy.setSeverity("HIGH");
        savedAllergy.setReaction("Rash");

        when(allergyRepository.save(any(Allergy.class))).thenReturn(savedAllergy);

        // Act - Add second allergy
        Allergy result = allergyService.addSingleAllergy(patientId, newAllergy);

        // Assert - Verify new allergy was added (not replacing existing)
        assertNotNull(result);
        assertEquals("Penicillin", result.getSubstance());
        assertEquals(patientId, result.getPatientId());

        // Verify existing allergies were checked
        verify(allergyRepository, times(1)).findAllByPatientId(patientId);
        verify(allergyRepository, times(1)).save(any(Allergy.class));
    }

    @Test
    void addSingleAllergy_patientIdIsSet_successfullyAssociatesWithPatient() {
        // Arrange - Verify patient ID is correctly associated
        Long patientId = 456L; // Different patient ID
        
        Allergy newAllergy = new Allergy();
        newAllergy.setSubstance("Latex");
        newAllergy.setSeverity("MEDIUM");
        newAllergy.setReaction("Skin irritation");
        // Note: patientId not set initially, service should set it

        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());

        Allergy savedAllergy = new Allergy();
        savedAllergy.setId(4L);
        savedAllergy.setPatientId(patientId);
        savedAllergy.setSubstance("Latex");
        savedAllergy.setSeverity("MEDIUM");
        savedAllergy.setReaction("Skin irritation");

        when(allergyRepository.save(any(Allergy.class))).thenReturn(savedAllergy);

        // Act
        Allergy result = allergyService.addSingleAllergy(patientId, newAllergy);

        // Assert - Verify patient ID was set correctly
        assertEquals(patientId, result.getPatientId(), 
                "Patient ID should be set to the patient for whom allergy is being created");
        assertEquals("Latex", result.getSubstance());

        verify(allergyRepository, times(1)).save(any(Allergy.class));
    }

    @Test
    void addSingleAllergy_allergyAppearsInPatientList_verifyListRetrieval() {
        // Arrange - Test that allergy appears in patient's allergy list after creation
        Long patientId = 123L;
        
        Allergy newAllergy = new Allergy();
        newAllergy.setSubstance("Eggs");
        newAllergy.setSeverity("MEDIUM");
        newAllergy.setReaction("Digestive upset");

        // Initially empty list
        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());

        Allergy savedAllergy = new Allergy();
        savedAllergy.setId(5L);
        savedAllergy.setPatientId(patientId);
        savedAllergy.setSubstance("Eggs");
        savedAllergy.setSeverity("MEDIUM");
        savedAllergy.setReaction("Digestive upset");

        when(allergyRepository.save(any(Allergy.class))).thenReturn(savedAllergy);

        // Act - Create the allergy
        Allergy createdAllergy = allergyService.addSingleAllergy(patientId, newAllergy);

        // Simulate retrieving all allergies after creation
        List<Allergy> updatedList = new ArrayList<>();
        updatedList.add(createdAllergy);
        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(updatedList);

        List<Allergy> patientAllergies = allergyService.getAllergiesByPatientId(patientId);

        // Assert - Verify allergy appears in patient's list
        assertNotNull(patientAllergies);
        assertEquals(1, patientAllergies.size(), "Patient should have 1 allergy in their list");
        assertEquals("Eggs", patientAllergies.get(0).getSubstance());
        assertEquals(createdAllergy.getId(), patientAllergies.get(0).getId());

        verify(allergyRepository, times(2)).findAllByPatientId(patientId);
    }

    @Test
    void addSingleAllergy_duplicateSubstanceCaseInsensitive_throwsConflictException() {
        // Arrange - Test duplicate allergy detection (case-insensitive)
        Long patientId = 123L;
        
        // Existing allergy
        Allergy existingAllergy = new Allergy();
        existingAllergy.setId(1L);
        existingAllergy.setPatientId(patientId);
        existingAllergy.setSubstance("Peanuts");
        existingAllergy.setSeverity("HIGH");
        existingAllergy.setReaction("Anaphylaxis");

        List<Allergy> existingAllergies = new ArrayList<>();
        existingAllergies.add(existingAllergy);

        // New allergy with same substance but different case
        Allergy newAllergy = new Allergy();
        newAllergy.setSubstance("PEANUTS");  // Different case, but same substance
        newAllergy.setSeverity("MEDIUM");
        newAllergy.setReaction("Swelling");

        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(existingAllergies);

        // Act & Assert - Expect CONFLICT exception for duplicate
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                allergyService.addSingleAllergy(patientId, newAllergy)
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode(), 
                "Should return HTTP 409 CONFLICT for duplicate allergy");
        assertTrue(ex.getReason().contains("already exists"), 
                "Error message should mention allergy already exists");

        // Verify save was NEVER called
        verify(allergyRepository, never()).save(any(Allergy.class));
    }

    @Test
    void addSingleAllergy_duplicateSubstanceWithWhitespace_throwsConflictException() {
        // Arrange - Test duplicate detection with whitespace trimming
        Long patientId = 123L;
        
        Allergy existingAllergy = new Allergy();
        existingAllergy.setId(1L);
        existingAllergy.setPatientId(patientId);
        existingAllergy.setSubstance("  Shellfish  ");  // With extra whitespace
        existingAllergy.setSeverity("HIGH");
        existingAllergy.setReaction("Anaphylaxis");

        List<Allergy> existingAllergies = new ArrayList<>();
        existingAllergies.add(existingAllergy);

        Allergy newAllergy = new Allergy();
        newAllergy.setSubstance("Shellfish");  // Without whitespace
        newAllergy.setSeverity("HIGH");
        newAllergy.setReaction("Anaphylaxis");

        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(existingAllergies);

        // Act & Assert
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                allergyService.addSingleAllergy(patientId, newAllergy)
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertTrue(ex.getReason().contains("already exists"));

        verify(allergyRepository, never()).save(any(Allergy.class));
    }

    @Test
    void addSingleAllergy_nullSubstanceInExisting_allowsAddition() {
        // Arrange - Test that null substance in existing allergy doesn't block addition
        Long patientId = 123L;
        
        Allergy existingAllergyWithNullSubstance = new Allergy();
        existingAllergyWithNullSubstance.setId(1L);
        existingAllergyWithNullSubstance.setPatientId(patientId);
        existingAllergyWithNullSubstance.setSubstance(null);  // Null substance
        existingAllergyWithNullSubstance.setSeverity("HIGH");
        existingAllergyWithNullSubstance.setReaction("Anaphylaxis");

        List<Allergy> existingAllergies = new ArrayList<>();
        existingAllergies.add(existingAllergyWithNullSubstance);

        Allergy newAllergy = new Allergy();
        newAllergy.setSubstance("Peanuts");
        newAllergy.setSeverity("HIGH");
        newAllergy.setReaction("Anaphylaxis");

        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(existingAllergies);

        Allergy savedAllergy = new Allergy();
        savedAllergy.setId(2L);
        savedAllergy.setPatientId(patientId);
        savedAllergy.setSubstance("Peanuts");
        savedAllergy.setSeverity("HIGH");
        savedAllergy.setReaction("Anaphylaxis");

        when(allergyRepository.save(any(Allergy.class))).thenReturn(savedAllergy);

        // Act - Should successfully add because existing substance is null
        Allergy result = allergyService.addSingleAllergy(patientId, newAllergy);

        // Assert
        assertNotNull(result);
        assertEquals("Peanuts", result.getSubstance());

        verify(allergyRepository, times(1)).save(any(Allergy.class));
    }

    @Test
    void addSingleAllergy_nullSubstanceInNewAllergy_allowsAddition() {
        // Arrange - Test that null substance in new allergy is allowed
        Long patientId = 123L;
        
        Allergy existingAllergy = new Allergy();
        existingAllergy.setId(1L);
        existingAllergy.setPatientId(patientId);
        existingAllergy.setSubstance("Peanuts");
        existingAllergy.setSeverity("HIGH");
        existingAllergy.setReaction("Anaphylaxis");

        List<Allergy> existingAllergies = new ArrayList<>();
        existingAllergies.add(existingAllergy);

        Allergy newAllergy = new Allergy();
        newAllergy.setSubstance(null);  // Null substance in new allergy
        newAllergy.setSeverity("LOW");
        newAllergy.setReaction("Mild reaction");

        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(existingAllergies);

        Allergy savedAllergy = new Allergy();
        savedAllergy.setId(2L);
        savedAllergy.setPatientId(patientId);
        savedAllergy.setSubstance(null);
        savedAllergy.setSeverity("LOW");
        savedAllergy.setReaction("Mild reaction");

        when(allergyRepository.save(any(Allergy.class))).thenReturn(savedAllergy);

        // Act - Should successfully add because new substance is null
        Allergy result = allergyService.addSingleAllergy(patientId, newAllergy);

        // Assert
        assertNotNull(result);
        assertNull(result.getSubstance());

        verify(allergyRepository, times(1)).save(any(Allergy.class));
    }
}

