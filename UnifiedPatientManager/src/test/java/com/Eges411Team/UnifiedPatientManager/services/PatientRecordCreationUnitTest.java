package com.Eges411Team.UnifiedPatientManager.services;

import com.Eges411Team.UnifiedPatientManager.DTOs.responses.PatientRecordDTO;
import com.Eges411Team.UnifiedPatientManager.entity.Allergy;
import com.Eges411Team.UnifiedPatientManager.entity.Role;
import com.Eges411Team.UnifiedPatientManager.entity.User;
import com.Eges411Team.UnifiedPatientManager.repositories.AllergyRepository;
import com.Eges411Team.UnifiedPatientManager.repositories.MedicalHistoryRepo;
import com.Eges411Team.UnifiedPatientManager.repositories.MedicationRepository;
import com.Eges411Team.UnifiedPatientManager.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientRecordCreationUnitTest {

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
        template.setUsername("seb_eucalyptus");
        template.setFirstName("Sebastian");
        template.setLastName("Eucalyptus");
        template.setEmail("seb@example.com");
        template.setPhoneNumber("444-444-4445");
        template.setAddress("1 White House, Washington D.C, USA, 01222");
        template.setDateOfBirth(LocalDateTime.of(1996,4,26,0,0));
        template.setGender("Male");
        template.setRole(Role.DOCTOR); // role needs a value
    }

    @Test
    void createPatientRecord_success_populatesDtoAndPersistsAllergies() {
        // Allergies provided in input
        Allergy penicillin = new Allergy();
        penicillin.setId(100L);
        penicillin.setSubstance("Penicillin");
        penicillin.setReaction("Rash");
        penicillin.setSeverity("High");

        Allergy amoxicillin = new Allergy();
        amoxicillin.setId(101L);
        amoxicillin.setSubstance("Amoxicillin");
        amoxicillin.setReaction("Hives");
        amoxicillin.setSeverity("Moderate");

        // Simulate repository assigning an ID on save
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

        when(userRepository.save(template)).thenReturn(persisted);
        when(allergyRepository.save(any(Allergy.class))).thenAnswer(inv -> (Allergy) inv.getArgument(0));

        PatientRecordDTO dto = patientRecordService.createPatientRecord(template, List.of(penicillin, amoxicillin));

        assertThat(dto.getPatientId()).isEqualTo(999L);
        assertThat(dto.getFirstName()).isEqualTo("Sebastian");
        assertThat(dto.getLastName()).isEqualTo("Eucalyptus");
        assertThat(dto.getEmail()).isEqualTo("seb@example.com");
        assertThat(dto.getPhoneNumber()).isEqualTo("444-444-4445");
        assertThat(dto.getAllergies()).hasSize(2);
        assertThat(dto.getAllergies()).extracting("substance")
            .containsExactlyInAnyOrder("Penicillin", "Amoxicillin");

        // Verify allergies saved with patient id
        ArgumentCaptor<Allergy> allergyCaptor = ArgumentCaptor.forClass(Allergy.class);
        verify(allergyRepository, times(2)).save(allergyCaptor.capture());
        assertThat(allergyCaptor.getAllValues())
            .allMatch(a -> a.getPatientId() != null && a.getPatientId().equals(999L));
    }

    @Test
    void createPatientRecord_missingUsername_throws() {
        template.setUsername(null);
        try {
            patientRecordService.createPatientRecord(template, List.of());
        } catch (RuntimeException ex) {
            assertThat(ex.getMessage()).contains("Username is required");
            verifyNoInteractions(userRepository);
        }
    }
}
