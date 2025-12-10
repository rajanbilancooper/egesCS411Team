package com.Eges411Team.UnifiedPatientManager.serviceUnitTests.patientRetrievalTests;

import com.Eges411Team.UnifiedPatientManager.DTOs.requests.PatientRecordUpdateDTO;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test Case 25: Edit patient record
 * Use Case: Retrieve Patient Record for update changes
 * Requirement: 3.1.1 - The system shall allow authorized users to create, view, update, 
 *              and edit patient records
 * Technique: Equivalence Partitioning - Testing the valid input class for updating an 
 *            existing patient record
 */
@ExtendWith(MockitoExtension.class)
class editPatientRecordTest {

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
    void updatePatientRecord_validUpdates_successfullyUpdatesPatient() {
        // Arrange - Create existing patient with original data
        Long patientId = 123L;
        
        User existingPatient = new User();
        existingPatient.setId(patientId);
        existingPatient.setFirstName("Rhea");
        existingPatient.setLastName("Thakur");
        existingPatient.setEmail("rhea.thakur@email.com");
        existingPatient.setPhoneNumber("555-1234");
        existingPatient.setAddress("123 Main St, Boston, MA 02101");
        existingPatient.setDateOfBirth(LocalDateTime.of(1990, 5, 15, 0, 0));

        // Create update DTO with new values
        PatientRecordUpdateDTO updateDTO = new PatientRecordUpdateDTO();
        updateDTO.setAddress("500 Commonwealth Ave, Boston, MA 02215");
        updateDTO.setPhoneNumber("617-333-3333");
        updateDTO.setEmail("rhea@gmail.com");

        // Mock repository responses
        when(userRepository.findById(patientId)).thenReturn(Optional.of(existingPatient));
        when(userRepository.findAllByEmail("rhea@gmail.com")).thenReturn(new ArrayList<>());
        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicalHistoryRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicationRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());

        // Mock save to verify updated fields
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            return savedUser;
        });

        // Act - Doctor updates the patient record
        ResponseEntity<PatientRecordDTO> response = patientRecordService.updatePatientRecord(patientId, updateDTO);

        // Assert - Verify the update was successful
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Should return HTTP 200 OK");
        
        PatientRecordDTO result = response.getBody();
        assertNotNull(result, "Response body should not be null");
        
        // Verify updated fields
        assertEquals("500 Commonwealth Ave, Boston, MA 02215", result.getAddress(), 
                "Address should be updated");
        assertEquals("617-333-3333", result.getPhoneNumber(), 
                "Phone number should be updated");
        assertEquals("rhea@gmail.com", result.getEmail(), 
                "Email should be updated");
        
        // Verify unchanged fields remain the same
        assertEquals("Rhea", result.getFirstName(), "First name should remain unchanged");
        assertEquals("Thakur", result.getLastName(), "Last name should remain unchanged");

        // Verify repository methods were called
        verify(userRepository, times(1)).findById(patientId);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updatePatientRecord_updateAddressOnly_successfullyUpdatesAddress() {
        // Arrange - Test partial update (equivalence partitioning - single field update)
        Long patientId = 123L;
        
        User existingPatient = new User();
        existingPatient.setId(patientId);
        existingPatient.setFirstName("Rhea");
        existingPatient.setLastName("Thakur");
        existingPatient.setEmail("rhea.thakur@email.com");
        existingPatient.setPhoneNumber("555-1234");
        existingPatient.setAddress("123 Main St, Boston, MA 02101");

        PatientRecordUpdateDTO updateDTO = new PatientRecordUpdateDTO();
        updateDTO.setAddress("500 Commonwealth Ave, Boston, MA 02215");
        // Only address is set, other fields are null

        when(userRepository.findById(patientId)).thenReturn(Optional.of(existingPatient));
        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicalHistoryRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicationRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ResponseEntity<PatientRecordDTO> response = patientRecordService.updatePatientRecord(patientId, updateDTO);

        // Assert
        PatientRecordDTO result = response.getBody();
        assertNotNull(result);
        assertEquals("500 Commonwealth Ave, Boston, MA 02215", result.getAddress());
        // Other fields should remain unchanged
        assertEquals("rhea.thakur@email.com", result.getEmail());
        assertEquals("555-1234", result.getPhoneNumber());

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updatePatientRecord_updatePhoneOnly_successfullyUpdatesPhone() {
        // Arrange - Test updating phone number only
        Long patientId = 123L;
        
        User existingPatient = new User();
        existingPatient.setId(patientId);
        existingPatient.setFirstName("Rhea");
        existingPatient.setLastName("Thakur");
        existingPatient.setEmail("rhea.thakur@email.com");
        existingPatient.setPhoneNumber("555-1234");
        existingPatient.setAddress("123 Main St, Boston, MA 02101");

        PatientRecordUpdateDTO updateDTO = new PatientRecordUpdateDTO();
        updateDTO.setPhoneNumber("617-333-3333");

        when(userRepository.findById(patientId)).thenReturn(Optional.of(existingPatient));
        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicalHistoryRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicationRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ResponseEntity<PatientRecordDTO> response = patientRecordService.updatePatientRecord(patientId, updateDTO);

        // Assert
        PatientRecordDTO result = response.getBody();
        assertNotNull(result);
        assertEquals("617-333-3333", result.getPhoneNumber());
        // Other fields remain unchanged
        assertEquals("rhea.thakur@email.com", result.getEmail());
        assertEquals("123 Main St, Boston, MA 02101", result.getAddress());

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updatePatientRecord_updateEmailOnly_successfullyUpdatesEmail() {
        // Arrange - Test updating email only
        Long patientId = 123L;
        
        User existingPatient = new User();
        existingPatient.setId(patientId);
        existingPatient.setFirstName("Rhea");
        existingPatient.setLastName("Thakur");
        existingPatient.setEmail("rhea.thakur@email.com");
        existingPatient.setPhoneNumber("555-1234");
        existingPatient.setAddress("123 Main St, Boston, MA 02101");

        PatientRecordUpdateDTO updateDTO = new PatientRecordUpdateDTO();
        updateDTO.setEmail("rhea@gmail.com");

        when(userRepository.findById(patientId)).thenReturn(Optional.of(existingPatient));
        when(userRepository.findAllByEmail("rhea@gmail.com")).thenReturn(new ArrayList<>());
        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicalHistoryRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicationRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ResponseEntity<PatientRecordDTO> response = patientRecordService.updatePatientRecord(patientId, updateDTO);

        // Assert
        PatientRecordDTO result = response.getBody();
        assertNotNull(result);
        assertEquals("rhea@gmail.com", result.getEmail());
        // Other fields remain unchanged
        assertEquals("555-1234", result.getPhoneNumber());
        assertEquals("123 Main St, Boston, MA 02101", result.getAddress());

        verify(userRepository, times(1)).findAllByEmail("rhea@gmail.com");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updatePatientRecord_reopenRecord_showsUpdatedDetails() {
        // Arrange - Test that reopening the record shows updated values
        Long patientId = 123L;
        
        User existingPatient = new User();
        existingPatient.setId(patientId);
        existingPatient.setFirstName("Rhea");
        existingPatient.setLastName("Thakur");
        existingPatient.setEmail("rhea.thakur@email.com");
        existingPatient.setPhoneNumber("555-1234");
        existingPatient.setAddress("123 Main St, Boston, MA 02101");

        PatientRecordUpdateDTO updateDTO = new PatientRecordUpdateDTO();
        updateDTO.setAddress("500 Commonwealth Ave, Boston, MA 02215");
        updateDTO.setPhoneNumber("617-333-3333");
        updateDTO.setEmail("rhea@gmail.com");

        when(userRepository.findById(patientId)).thenReturn(Optional.of(existingPatient));
        when(userRepository.findAllByEmail("rhea@gmail.com")).thenReturn(new ArrayList<>());
        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicalHistoryRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicationRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());

        // Mock save to actually update the object
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            // Simulate the user being saved and then retrieved again
            existingPatient.setAddress(savedUser.getAddress());
            existingPatient.setPhoneNumber(savedUser.getPhoneNumber());
            existingPatient.setEmail(savedUser.getEmail());
            return savedUser;
        });

        // Act - First update
        patientRecordService.updatePatientRecord(patientId, updateDTO);
        
        // Simulate reopening the record - retrieve the patient again
        ResponseEntity<PatientRecordDTO> reopenResponse = patientRecordService.updatePatientRecord(
            patientId, new PatientRecordUpdateDTO()
        );

        // Assert - Verify reopened record shows updated details
        PatientRecordDTO reopenedRecord = reopenResponse.getBody();
        assertNotNull(reopenedRecord);
        assertEquals("500 Commonwealth Ave, Boston, MA 02215", reopenedRecord.getAddress(), 
                "Reopened record should show updated address");
        assertEquals("617-333-3333", reopenedRecord.getPhoneNumber(), 
                "Reopened record should show updated phone");
        assertEquals("rhea@gmail.com", reopenedRecord.getEmail(), 
                "Reopened record should show updated email");

        // Verify save was called for the update
        verify(userRepository, atLeast(1)).save(any(User.class));
    }

    @Test
    void updatePatientRecord_updateHeightAndWeight_successfullyUpdates() {
        // Arrange - Test updating anthropometric data (equivalence partitioning - optional fields)
        Long patientId = 123L;
        
        User existingPatient = new User();
        existingPatient.setId(patientId);
        existingPatient.setFirstName("Rhea");
        existingPatient.setLastName("Thakur");
        existingPatient.setEmail("rhea.thakur@email.com");
        existingPatient.setPhoneNumber("555-1234");
        existingPatient.setAddress("123 Main St, Boston, MA 02101");
        existingPatient.setHeight("5'5\"");
        existingPatient.setWeight("130 lbs");

        PatientRecordUpdateDTO updateDTO = new PatientRecordUpdateDTO();
        updateDTO.setHeight("5'6\"");
        updateDTO.setWeight("135 lbs");

        when(userRepository.findById(patientId)).thenReturn(Optional.of(existingPatient));
        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicalHistoryRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicationRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ResponseEntity<PatientRecordDTO> response = patientRecordService.updatePatientRecord(patientId, updateDTO);

        // Assert
        PatientRecordDTO result = response.getBody();
        assertNotNull(result);
        assertEquals("5'6\"", result.getHeight());
        assertEquals("135 lbs", result.getWeight());

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updatePatientRecord_patientNotFound_throwsException() {
        // Arrange - Test patient not found (orElseThrow branch)
        Long nonExistentPatientId = 999L;

        PatientRecordUpdateDTO updateDTO = new PatientRecordUpdateDTO();
        updateDTO.setEmail("newemail@email.com");

        when(userRepository.findById(nonExistentPatientId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                patientRecordService.updatePatientRecord(nonExistentPatientId, updateDTO)
        );

        assertTrue(ex.getMessage().contains("User not found"));

        verify(userRepository, times(1)).findById(nonExistentPatientId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updatePatientRecord_duplicateEmailExists_throwsException() {
        // Arrange - Test duplicate email validation (email changed & isDuplicate true)
        Long patientId = 123L;

        User existingPatient = new User();
        existingPatient.setId(patientId);
        existingPatient.setFirstName("Rhea");
        existingPatient.setLastName("Thakur");
        existingPatient.setEmail("rhea.thakur@email.com");
        existingPatient.setPhoneNumber("555-1234");

        PatientRecordUpdateDTO updateDTO = new PatientRecordUpdateDTO();
        updateDTO.setEmail("existing@email.com");  // Try to use existing email

        // Mock: patient exists, but email is already taken by another user
        when(userRepository.findById(patientId)).thenReturn(Optional.of(existingPatient));

        User otherUser = new User();
        otherUser.setId(456L);  // Different user
        otherUser.setEmail("existing@email.com");

        when(userRepository.findAllByEmail("existing@email.com"))
                .thenReturn(Arrays.asList(otherUser));  // Email belongs to different user

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                patientRecordService.updatePatientRecord(patientId, updateDTO)
        );

        assertTrue(ex.getMessage().contains("already exists"));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updatePatientRecord_duplicateNameAndDOB_throwsException() {
        // Arrange - Test duplicate name+DOB validation
        Long patientId = 123L;

        User existingPatient = new User();
        existingPatient.setId(patientId);
        existingPatient.setFirstName("Rhea");
        existingPatient.setLastName("Thakur");
        existingPatient.setEmail("rhea@email.com");
        existingPatient.setDateOfBirth(LocalDateTime.of(1990, 5, 15, 0, 0));

        PatientRecordUpdateDTO updateDTO = new PatientRecordUpdateDTO();
        updateDTO.setFirstName("John");  // Change first name

        // Mock: patient exists, but new name+DOB already exists for different patient
        when(userRepository.findById(patientId)).thenReturn(Optional.of(existingPatient));

        User duplicatePatient = new User();
        duplicatePatient.setId(999L);  // Different patient
        duplicatePatient.setFirstName("John");
        duplicatePatient.setLastName("Thakur");
        duplicatePatient.setDateOfBirth(LocalDateTime.of(1990, 5, 15, 0, 0));

        when(userRepository.findByFirstNameAndLastNameAndDateOfBirth("John", "Thakur", existingPatient.getDateOfBirth()))
                .thenReturn(Optional.of(duplicatePatient));

        // Act & Assert - Exception thrown before repo calls, so no need to mock them
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                patientRecordService.updatePatientRecord(patientId, updateDTO)
        );

        assertTrue(ex.getMessage().contains("already exists"));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updatePatientRecord_changeLastNameToDuplicate_throwsException() {
        // Arrange - Test duplicate detection when only lastName changes
        Long patientId = 123L;

        User existingPatient = new User();
        existingPatient.setId(patientId);
        existingPatient.setFirstName("Rhea");
        existingPatient.setLastName("Thakur");
        existingPatient.setEmail("rhea@email.com");
        existingPatient.setDateOfBirth(LocalDateTime.of(1990, 5, 15, 0, 0));

        PatientRecordUpdateDTO updateDTO = new PatientRecordUpdateDTO();
        updateDTO.setLastName("Smith");  // Change only last name

        when(userRepository.findById(patientId)).thenReturn(Optional.of(existingPatient));

        User duplicatePatient = new User();
        duplicatePatient.setId(456L);
        duplicatePatient.setFirstName("Rhea");
        duplicatePatient.setLastName("Smith");
        duplicatePatient.setDateOfBirth(LocalDateTime.of(1990, 5, 15, 0, 0));

        when(userRepository.findByFirstNameAndLastNameAndDateOfBirth("Rhea", "Smith", existingPatient.getDateOfBirth()))
                .thenReturn(Optional.of(duplicatePatient));

        // Act & Assert - Exception thrown before repo calls
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                patientRecordService.updatePatientRecord(patientId, updateDTO)
        );

        assertTrue(ex.getMessage().contains("already exists"));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updatePatientRecord_emailNotChanged_bypassesDuplicateCheck() {
        // Arrange - Test that email check is bypassed when email isn't changed
        Long patientId = 123L;

        User existingPatient = new User();
        existingPatient.setId(patientId);
        existingPatient.setFirstName("Rhea");
        existingPatient.setLastName("Thakur");
        existingPatient.setEmail("rhea@email.com");
        existingPatient.setPhoneNumber("555-1234");

        PatientRecordUpdateDTO updateDTO = new PatientRecordUpdateDTO();
        updateDTO.setEmail("rhea@email.com");  // Same email, not changed

        when(userRepository.findById(patientId)).thenReturn(Optional.of(existingPatient));
        when(allergyRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicalHistoryRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(medicationRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ResponseEntity<PatientRecordDTO> response = patientRecordService.updatePatientRecord(patientId, updateDTO);

        // Assert - Should succeed without checking for duplicate
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // findAllByEmail should NOT be called because email wasn't changed
        verify(userRepository, never()).findAllByEmail(any());
        verify(userRepository, times(1)).save(any(User.class));
    }
}

