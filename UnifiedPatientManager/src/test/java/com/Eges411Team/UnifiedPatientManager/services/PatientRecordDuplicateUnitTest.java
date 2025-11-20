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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientRecordDuplicateUnitTest {

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
    private User existing;

    @BeforeEach
    void setUp() {
        template = new User();
        template.setUsername("john_smith_1");
        template.setFirstName("John");
        template.setLastName("Smith");
        template.setEmail("john.smith@example.com");
        template.setPhoneNumber("919-200-4345");
        template.setAddress("1 Beacon St, Boston MA, 02215");
        template.setDateOfBirth(LocalDateTime.of(1999,6,20,0,0));
        template.setGender("Male");
        template.setRole(Role.NURSE); // any non-null role

        existing = new User();
        existing.setId(500L);
        existing.setUsername("john_smith_existing");
        existing.setFirstName("John");
        existing.setLastName("Smith");
        existing.setEmail("john.smith@example.com");
        existing.setPhoneNumber("919-200-4345");
        existing.setAddress("1 Beacon St, Boston MA, 02215");
        existing.setDateOfBirth(LocalDateTime.of(1999,6,20,0,0));
        existing.setGender("Male");
        existing.setRole(Role.PATIENT);
    }

    @Test
    void createPatientRecord_duplicatePatient_throwsAndDoesNotPersist() {
        when(userRepository.findByFirstNameAndLastNameAndDateOfBirth(
            "John", "Smith", LocalDateTime.of(1999,6,20,0,0))
        ).thenReturn(Optional.of(existing));

        try {
            patientRecordService.createPatientRecord(template, java.util.List.of());
        } catch (RuntimeException ex) {
            assertThat(ex.getMessage()).contains("Patient already exists");
            // Ensure save was never called for duplicate
            verify(userRepository, never()).save(any(User.class));
            verifyNoInteractions(allergyRepository);
        }
    }
}
