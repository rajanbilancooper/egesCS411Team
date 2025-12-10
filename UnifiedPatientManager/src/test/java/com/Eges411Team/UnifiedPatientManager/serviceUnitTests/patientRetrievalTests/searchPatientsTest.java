package com.Eges411Team.UnifiedPatientManager.serviceUnitTests.patientRetrievalTests;

import com.Eges411Team.UnifiedPatientManager.DTOs.responses.PatientRecordDTO;
import com.Eges411Team.UnifiedPatientManager.entity.User;
import com.Eges411Team.UnifiedPatientManager.repositories.AllergyRepository;
import com.Eges411Team.UnifiedPatientManager.repositories.MedicalHistoryRepo;
import com.Eges411Team.UnifiedPatientManager.repositories.MedicationRepository;
import com.Eges411Team.UnifiedPatientManager.repositories.UserRepository;
import com.Eges411Team.UnifiedPatientManager.services.PatientRecordService;
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
 * Test Case: Search patients by partial name
 * Use Case: Search for patients in the system
 * Requirement: 3.1.1 - The system shall allow authorized users to search patient records
 * Technique: Equivalence Partitioning - Testing various search query inputs
 */
@ExtendWith(MockitoExtension.class)
class searchPatientsTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AllergyRepository allergyRepository;

    @Mock
    private MedicalHistoryRepo medicalHistoryRepository;

    @Mock
    private MedicationRepository medicationRepository;

    @InjectMocks
    private PatientRecordService patientRecordService;

    @Test
    void searchPatients_nullQuery_returnsEmptyList() {
        // Act
        List<PatientRecordDTO> result = patientRecordService.searchPatients(null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository, never()).findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(anyString(), anyString());
    }

    @Test
    void searchPatients_emptyQuery_returnsEmptyList() {
        // Act
        List<PatientRecordDTO> result = patientRecordService.searchPatients("");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository, never()).findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(anyString(), anyString());
    }

    @Test
    void searchPatients_whitespaceQuery_returnsEmptyList() {
        // Act
        List<PatientRecordDTO> result = patientRecordService.searchPatients("   ");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository, never()).findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(anyString(), anyString());
    }

    @Test
    void searchPatients_noMatchingUsers_returnsEmptyList() {
        // Arrange
        when(userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("XYZ", "XYZ"))
            .thenReturn(new ArrayList<>());

        // Act
        List<PatientRecordDTO> result = patientRecordService.searchPatients("XYZ");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository).findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("XYZ", "XYZ");
    }

    @Test
    void searchPatients_validQuery_returnsMatchingUsers() {
        // Arrange
        User user1 = new User();
        user1.setId(1L);
        user1.setFirstName("John");
        user1.setLastName("Smith");
        user1.setEmail("john.smith@email.com");
        user1.setPhoneNumber("555-1111");

        User user2 = new User();
        user2.setId(2L);
        user2.setFirstName("Jane");
        user2.setLastName("Johnson");
        user2.setEmail("jane.j@email.com");
        user2.setPhoneNumber("555-2222");

        when(userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("Jo", "Jo"))
            .thenReturn(Arrays.asList(user1, user2));

        // Act
        List<PatientRecordDTO> result = patientRecordService.searchPatients("Jo");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        
        assertEquals(1L, result.get(0).getPatientId());
        assertEquals("John", result.get(0).getFirstName());
        assertEquals("Smith", result.get(0).getLastName());
        
        assertEquals(2L, result.get(1).getPatientId());
        assertEquals("Jane", result.get(1).getFirstName());
        assertEquals("Johnson", result.get(1).getLastName());
    }

    @Test
    void searchPatients_caseInsensitiveSearch_returnsMatches() {
        // Arrange
        User user = new User();
        user.setId(3L);
        user.setFirstName("Alice");
        user.setLastName("Anderson");
        user.setEmail("alice@email.com");
        user.setPhoneNumber("555-3333");

        when(userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("ali", "ali"))
            .thenReturn(Arrays.asList(user));

        // Act
        List<PatientRecordDTO> result = patientRecordService.searchPatients("ali");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Alice", result.get(0).getFirstName());
    }

    @Test
    void searchPatients_queryWithExtraSpaces_trimsAndSearches() {
        // Arrange
        User user = new User();
        user.setId(4L);
        user.setFirstName("Bob");
        user.setLastName("Brown");
        user.setEmail("bob@email.com");
        user.setPhoneNumber("555-4444");

        when(userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("Bob", "Bob"))
            .thenReturn(Arrays.asList(user));

        // Act
        List<PatientRecordDTO> result = patientRecordService.searchPatients("  Bob  ");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Bob", result.get(0).getFirstName());
        verify(userRepository).findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("Bob", "Bob");
    }

    @Test
    void searchPatients_matchLastName_returnsUser() {
        // Arrange
        User user = new User();
        user.setId(5L);
        user.setFirstName("Charlie");
        user.setLastName("Williams");
        user.setEmail("charlie@email.com");
        user.setPhoneNumber("555-5555");

        when(userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("Will", "Will"))
            .thenReturn(Arrays.asList(user));

        // Act
        List<PatientRecordDTO> result = patientRecordService.searchPatients("Will");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Williams", result.get(0).getLastName());
    }
}
