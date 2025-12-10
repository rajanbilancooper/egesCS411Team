package com.Eges411Team.UnifiedPatientManager.serviceUnitTests.patientRetrievalTests;

import com.Eges411Team.UnifiedPatientManager.DTOs.responses.PatientRecordDTO;
import com.Eges411Team.UnifiedPatientManager.entity.User;
import com.Eges411Team.UnifiedPatientManager.repositories.UserRepository;
import com.Eges411Team.UnifiedPatientManager.services.PatientRecordService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test Case 22: Search for patient found
 * Use Case: Retrieve Patient Record for Update changes
 * Requirement: 3.1.6 - The system shall enable doctors to search for patient records using 
 *              name, ID, or date of birth
 * Technique: Equivalence Partitioning - Testing the valid input class where a searched patient 
 *            ID exists in the system
 */
@ExtendWith(MockitoExtension.class)
class searchPatientFoundTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PatientRecordService patientRecordService;

    @Test
    void searchPatients_validPatientNameExists_returnsPatientRecord() {
        // Arrange - Create a patient that exists in the system
        User existingPatient = new User();
        existingPatient.setId(1L);
        existingPatient.setFirstName("Rhea");
        existingPatient.setLastName("Thakur");
        existingPatient.setEmail("rhea.thakur@email.com");
        existingPatient.setPhoneNumber("555-1234");
        existingPatient.setDateOfBirth(LocalDateTime.of(1990, 5, 15, 0, 0));

        // Mock repository to return the patient when searching by name
        when(userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("Rhea Thakur", "Rhea Thakur"))
                .thenReturn(Arrays.asList(existingPatient));

        // Act - Doctor searches for patient by name "Rhea Thakur"
        List<PatientRecordDTO> results = patientRecordService.searchPatients("Rhea Thakur");

        // Assert - Verify patient record is retrieved successfully
        assertNotNull(results, "Search results should not be null");
        assertFalse(results.isEmpty(), "Search results should not be empty");
        assertEquals(1, results.size(), "Should return exactly one patient record");

        PatientRecordDTO foundPatient = results.get(0);
        assertEquals(1L, foundPatient.getPatientId(), "Patient ID should match");
        assertEquals("Rhea", foundPatient.getFirstName(), "First name should match");
        assertEquals("Thakur", foundPatient.getLastName(), "Last name should match");
        assertEquals("rhea.thakur@email.com", foundPatient.getEmail(), "Email should match");
        assertEquals("555-1234", foundPatient.getPhoneNumber(), "Phone number should match");

        // Verify repository method was called with correct parameters
        verify(userRepository, times(1))
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("Rhea Thakur", "Rhea Thakur");
    }

    @Test
    void searchPatients_searchByFirstNameOnly_returnsPatientRecord() {
        // Arrange - Create a patient that exists in the system
        User existingPatient = new User();
        existingPatient.setId(2L);
        existingPatient.setFirstName("Rhea");
        existingPatient.setLastName("Thakur");
        existingPatient.setEmail("rhea.thakur@email.com");
        existingPatient.setPhoneNumber("555-1234");

        // Mock repository to return the patient when searching by first name only
        when(userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("Rhea", "Rhea"))
                .thenReturn(Arrays.asList(existingPatient));

        // Act - Doctor searches for patient by first name only
        List<PatientRecordDTO> results = patientRecordService.searchPatients("Rhea");

        // Assert - Verify patient record is retrieved
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Rhea", results.get(0).getFirstName());
        assertEquals("Thakur", results.get(0).getLastName());

        verify(userRepository, times(1))
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("Rhea", "Rhea");
    }

    @Test
    void searchPatients_searchByLastNameOnly_returnsPatientRecord() {
        // Arrange - Create a patient that exists in the system
        User existingPatient = new User();
        existingPatient.setId(3L);
        existingPatient.setFirstName("Rhea");
        existingPatient.setLastName("Thakur");
        existingPatient.setEmail("rhea.thakur@email.com");
        existingPatient.setPhoneNumber("555-1234");

        // Mock repository to return the patient when searching by last name only
        when(userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("Thakur", "Thakur"))
                .thenReturn(Arrays.asList(existingPatient));

        // Act - Doctor searches for patient by last name only
        List<PatientRecordDTO> results = patientRecordService.searchPatients("Thakur");

        // Assert - Verify patient record is retrieved
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Rhea", results.get(0).getFirstName());
        assertEquals("Thakur", results.get(0).getLastName());

        verify(userRepository, times(1))
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("Thakur", "Thakur");
    }

    @Test
    void searchPatients_caseInsensitiveSearch_returnsPatientRecord() {
        // Arrange - Test case insensitivity (equivalence partitioning - valid input variations)
        User existingPatient = new User();
        existingPatient.setId(4L);
        existingPatient.setFirstName("Rhea");
        existingPatient.setLastName("Thakur");
        existingPatient.setEmail("rhea.thakur@email.com");
        existingPatient.setPhoneNumber("555-1234");

        // Mock repository to return patient for lowercase search
        when(userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("rhea thakur", "rhea thakur"))
                .thenReturn(Arrays.asList(existingPatient));

        // Act - Doctor searches with lowercase
        List<PatientRecordDTO> results = patientRecordService.searchPatients("rhea thakur");

        // Assert - Verify patient is found regardless of case
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Rhea", results.get(0).getFirstName());
        assertEquals("Thakur", results.get(0).getLastName());

        verify(userRepository, times(1))
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("rhea thakur", "rhea thakur");
    }

    @Test
    void searchPatients_multipleMatchingPatients_returnsAllMatches() {
        // Arrange - Multiple patients with same name (equivalence partitioning - multiple valid results)
        User patient1 = new User();
        patient1.setId(5L);
        patient1.setFirstName("Rhea");
        patient1.setLastName("Thakur");
        patient1.setEmail("rhea.thakur1@email.com");
        patient1.setPhoneNumber("555-1234");

        User patient2 = new User();
        patient2.setId(6L);
        patient2.setFirstName("Rhea");
        patient2.setLastName("Kumar");
        patient2.setEmail("rhea.kumar@email.com");
        patient2.setPhoneNumber("555-5678");

        // Mock repository to return multiple patients
        when(userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("Rhea", "Rhea"))
                .thenReturn(Arrays.asList(patient1, patient2));

        // Act - Doctor searches for common first name
        List<PatientRecordDTO> results = patientRecordService.searchPatients("Rhea");

        // Assert - Verify all matching patients are returned
        assertNotNull(results);
        assertEquals(2, results.size(), "Should return both patients with first name Rhea");
        
        // Verify first patient
        assertEquals("Rhea", results.get(0).getFirstName());
        assertEquals("Thakur", results.get(0).getLastName());
        
        // Verify second patient
        assertEquals("Rhea", results.get(1).getFirstName());
        assertEquals("Kumar", results.get(1).getLastName());

        verify(userRepository, times(1))
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("Rhea", "Rhea");
    }

    @Test
    void searchPatients_partialNameMatch_returnsPatientRecord() {
        // Arrange - Test partial name matching (equivalence partitioning - partial valid input)
        User existingPatient = new User();
        existingPatient.setId(7L);
        existingPatient.setFirstName("Rhea");
        existingPatient.setLastName("Thakur");
        existingPatient.setEmail("rhea.thakur@email.com");
        existingPatient.setPhoneNumber("555-1234");

        // Mock repository to return patient for partial name search
        when(userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("Rhe", "Rhe"))
                .thenReturn(Arrays.asList(existingPatient));

        // Act - Doctor searches with partial name
        List<PatientRecordDTO> results = patientRecordService.searchPatients("Rhe");

        // Assert - Verify patient is found with partial match
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Rhea", results.get(0).getFirstName());
        assertEquals("Thakur", results.get(0).getLastName());

        verify(userRepository, times(1))
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("Rhe", "Rhe");
    }

    @Test
    void searchPatients_nullQuery_returnsEmptyList() {
        // Arrange - Test null query input (edge case)
        
        // Act - Search with null query
        List<PatientRecordDTO> results = patientRecordService.searchPatients(null);

        // Assert - Verify empty list is returned
        assertNotNull(results);
        assertTrue(results.isEmpty(), "Null query should return empty list");

        // Verify repository was NOT called for null query
        verify(userRepository, never()).findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(any(), any());
    }

    @Test
    void searchPatients_emptyQuery_returnsEmptyList() {
        // Arrange - Test empty string query

        // Act - Search with empty string
        List<PatientRecordDTO> results = patientRecordService.searchPatients("");

        // Assert - Verify empty list is returned
        assertNotNull(results);
        assertTrue(results.isEmpty(), "Empty query should return empty list");

        verify(userRepository, never()).findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(any(), any());
    }

    @Test
    void searchPatients_whitespaceOnlyQuery_returnsEmptyList() {
        // Arrange - Test whitespace-only query

        // Act - Search with only whitespace
        List<PatientRecordDTO> results = patientRecordService.searchPatients("   ");

        // Assert - Verify empty list is returned
        assertNotNull(results);
        assertTrue(results.isEmpty(), "Whitespace-only query should return empty list");

        verify(userRepository, never()).findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(any(), any());
    }

    @Test
    void searchPatients_validQueryButNoMatches_returnsEmptyList() {
        // Arrange - Test valid query that returns no matching patients
        String query = "NonExistentName";

        when(userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(query, query))
                .thenReturn(new ArrayList<>());  // No matches

        // Act - Search for non-existent patient
        List<PatientRecordDTO> results = patientRecordService.searchPatients(query);

        // Assert - Verify empty list is returned (not exception)
        assertNotNull(results);
        assertTrue(results.isEmpty(), "Should return empty list when no patients match");
        assertEquals(0, results.size());

        verify(userRepository, times(1))
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(query, query);
    }
}

