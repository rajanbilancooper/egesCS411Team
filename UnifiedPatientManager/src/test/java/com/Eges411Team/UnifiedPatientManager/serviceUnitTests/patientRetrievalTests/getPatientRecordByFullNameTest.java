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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test Case: Get patient record by full name - edge cases
 * Use Case: Retrieve Patient Record by Name
 * Requirement: 3.1.1 - The system shall allow authorized users to view patient records
 * Technique: Boundary Value Analysis and Equivalence Partitioning - Testing validation logic
 */
@ExtendWith(MockitoExtension.class)
class getPatientRecordByFullNameTest {

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
    void getPatientRecordByFullName_nullName_throwsException() {
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            patientRecordService.getPatientRecordByFullName(null);
        });
        
        assertEquals("Name must be provided", exception.getMessage());
        verify(userRepository, never()).findByFirstNameAndLastName(anyString(), anyString());
    }

    @Test
    void getPatientRecordByFullName_emptyName_throwsException() {
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            patientRecordService.getPatientRecordByFullName("   ");
        });
        
        assertEquals("Name must be provided", exception.getMessage());
        verify(userRepository, never()).findByFirstNameAndLastName(anyString(), anyString());
    }

    @Test
    void getPatientRecordByFullName_singleWord_throwsException() {
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            patientRecordService.getPatientRecordByFullName("John");
        });
        
        assertEquals("Full name must include first and last name", exception.getMessage());
        verify(userRepository, never()).findByFirstNameAndLastName(anyString(), anyString());
    }

    @Test
    void getPatientRecordByFullName_validNameWithMiddle_retrievesPatient() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Smith");
        user.setEmail("john.smith@email.com");
        user.setPhoneNumber("555-1234");
        user.setDateOfBirth(LocalDateTime.of(1990, 1, 1, 0, 0));

        when(userRepository.findByFirstNameAndLastName("John", "Smith"))
            .thenReturn(Optional.of(user));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(allergyRepository.findAllByPatientId(1L)).thenReturn(new ArrayList<>());
        when(medicationRepository.findAllByPatientId(1L)).thenReturn(new ArrayList<>());
        when(medicalHistoryRepository.findAllByPatientId(1L)).thenReturn(new ArrayList<>());

        // Act - Name with middle name: "John David Smith" should extract first="John", last="Smith"
        PatientRecordDTO result = patientRecordService.getPatientRecordByFullName("John David Smith");

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getPatientId());
        assertEquals("John", result.getFirstName());
        assertEquals("Smith", result.getLastName());
        verify(userRepository).findByFirstNameAndLastName("John", "Smith");
    }

    @Test
    void getPatientRecordByFullName_userNotFound_throwsException() {
        // Arrange
        when(userRepository.findByFirstNameAndLastName("Jane", "Doe"))
            .thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            patientRecordService.getPatientRecordByFullName("Jane Doe");
        });
        
        assertEquals("User not found with name: Jane Doe", exception.getMessage());
    }

    @Test
    void getPatientRecordByFullName_validTwoPartName_retrievesPatient() {
        // Arrange
        User user = new User();
        user.setId(2L);
        user.setFirstName("Alice");
        user.setLastName("Johnson");
        user.setEmail("alice.j@email.com");
        user.setPhoneNumber("555-5678");
        user.setDateOfBirth(LocalDateTime.of(1985, 6, 15, 0, 0));

        when(userRepository.findByFirstNameAndLastName("Alice", "Johnson"))
            .thenReturn(Optional.of(user));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(allergyRepository.findAllByPatientId(2L)).thenReturn(new ArrayList<>());
        when(medicationRepository.findAllByPatientId(2L)).thenReturn(new ArrayList<>());
        when(medicalHistoryRepository.findAllByPatientId(2L)).thenReturn(new ArrayList<>());

        // Act
        PatientRecordDTO result = patientRecordService.getPatientRecordByFullName("Alice Johnson");

        // Assert
        assertNotNull(result);
        assertEquals(2L, result.getPatientId());
        assertEquals("Alice", result.getFirstName());
        assertEquals("Johnson", result.getLastName());
    }
}
