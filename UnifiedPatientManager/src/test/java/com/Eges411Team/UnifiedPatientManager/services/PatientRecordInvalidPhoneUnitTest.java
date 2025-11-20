package com.Eges411Team.UnifiedPatientManager.services;

import com.Eges411Team.UnifiedPatientManager.entity.Role;
import com.Eges411Team.UnifiedPatientManager.entity.User;
import com.Eges411Team.UnifiedPatientManager.repositories.AllergyRepository;
import com.Eges411Team.UnifiedPatientManager.repositories.MedicalHistoryRepo;
import com.Eges411Team.UnifiedPatientManager.repositories.MedicationRepository;
import com.Eges411Team.UnifiedPatientManager.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Test Case #12: Invalid Data Format – Phone Number
 * Use Case: Storing Detailed Patient Information
 * Technique: Boundary Value Analysis – Testing whether the System will reject an incorrect format for a phone number.
 */
@ExtendWith(MockitoExtension.class)
class PatientRecordInvalidPhoneUnitTest {

    @Mock
    private AllergyRepository allergyRepository;
    @Mock
    private MedicationRepository medicationRepository;
    @Mock
    private MedicalHistoryRepo medicalHistoryRepo;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PatientRecordService patientRecordService;

    private User template;

    @BeforeEach
    void setUp() {
        template = new User();
        template.setUsername("N10235");
        template.setFirstName("John");
        template.setLastName("Smith");
        template.setEmail("john.smith@example.com");
        template.setAddress("1 White House, Washington DC, USA, 01111");
        template.setDateOfBirth(LocalDateTime.of(2000,6,24,0,0));
        template.setGender("Male");
        template.setRole(Role.PATIENT);
    }

    @Test
    void createPatientRecord_invalidPhoneFormat_throwsException() {
        // Given: phone number with only 5 digits (invalid format)
        template.setPhoneNumber("24536");

        // When/Then: attempt to create patient with invalid phone number
        assertThatThrownBy(() -> patientRecordService.createPatientRecord(template, List.of()))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Phone number must be 10 digits");

        // Verify: no persistence operations occurred
        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(allergyRepository);
    }

    @Test
    void createPatientRecord_validPhoneWithDashes_succeeds() {
        // Given: valid 10-digit phone with dashes
        template.setPhoneNumber("919-200-4345");

        User persisted = new User();
        persisted.setId(999L);
        persisted.setUsername(template.getUsername());
        persisted.setFirstName(template.getFirstName());
        persisted.setLastName(template.getLastName());
        persisted.setEmail(template.getEmail());
        persisted.setPhoneNumber(template.getPhoneNumber());
        persisted.setAddress(template.getAddress());
        persisted.setDateOfBirth(template.getDateOfBirth());
        persisted.setGender(template.getGender());
        persisted.setRole(template.getRole());

        when(userRepository.findByFirstNameAndLastNameAndDateOfBirth(
            anyString(), anyString(), any(LocalDateTime.class))
        ).thenReturn(java.util.Optional.empty());
        when(userRepository.save(template)).thenReturn(persisted);

        // When: create patient with valid phone
        var dto = patientRecordService.createPatientRecord(template, List.of());

        // Then: patient created successfully
        assertThat(dto.getPhoneNumber()).isEqualTo("919-200-4345");
        verify(userRepository).save(template);
    }

    @Test
    void createPatientRecord_validPhonePlainDigits_succeeds() {
        // Given: valid 10-digit phone without formatting
        template.setPhoneNumber("9192004345");

        User persisted = new User();
        persisted.setId(999L);
        persisted.setUsername(template.getUsername());
        persisted.setFirstName(template.getFirstName());
        persisted.setLastName(template.getLastName());
        persisted.setEmail(template.getEmail());
        persisted.setPhoneNumber(template.getPhoneNumber());
        persisted.setAddress(template.getAddress());
        persisted.setDateOfBirth(template.getDateOfBirth());
        persisted.setGender(template.getGender());
        persisted.setRole(template.getRole());

        when(userRepository.findByFirstNameAndLastNameAndDateOfBirth(
            anyString(), anyString(), any(LocalDateTime.class))
        ).thenReturn(java.util.Optional.empty());
        when(userRepository.save(template)).thenReturn(persisted);

        // When: create patient with valid phone
        var dto = patientRecordService.createPatientRecord(template, List.of());

        // Then: patient created successfully
        assertThat(dto.getPhoneNumber()).isEqualTo("9192004345");
        verify(userRepository).save(template);
    }

    @Test
    void createPatientRecord_missingPhoneNumber_throwsException() {
        // Given: null phone number
        template.setPhoneNumber(null);

        // When/Then: attempt to create patient without phone
        assertThatThrownBy(() -> patientRecordService.createPatientRecord(template, List.of()))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Phone number is required");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createPatientRecord_tooLongPhoneNumber_throwsException() {
        // Given: phone number with 15 digits (too long)
        template.setPhoneNumber("919-200-4345-999");

        // When/Then: attempt to create patient with too-long phone
        assertThatThrownBy(() -> patientRecordService.createPatientRecord(template, List.of()))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Phone number must be 10 digits");

        verify(userRepository, never()).save(any(User.class));
    }
}
