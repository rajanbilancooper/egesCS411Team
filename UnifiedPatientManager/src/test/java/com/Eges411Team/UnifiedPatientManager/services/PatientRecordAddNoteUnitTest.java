package com.Eges411Team.UnifiedPatientManager.services;

import com.Eges411Team.UnifiedPatientManager.DTOs.requests.PatientRecordUpdateDTO;
import com.Eges411Team.UnifiedPatientManager.DTOs.responses.PatientRecordDTO;
import com.Eges411Team.UnifiedPatientManager.entity.MedicalHistory;
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
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientRecordAddNoteUnitTest {

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

    private User existingUser;

    @BeforeEach
    void setup() {
        existingUser = new User();
        existingUser.setId(12345L);
        existingUser.setFirstName("John");
        existingUser.setLastName("Doe");
        existingUser.setUsername("john_doe");
        existingUser.setEmail("john@example.com");
        existingUser.setPhoneNumber("555-555-5555");
        existingUser.setAddress("123 Street");
        existingUser.setGender("Male");
        existingUser.setDateOfBirth(LocalDateTime.now().minusYears(30));
    }

    @Test
    void addTextNote_success_persistsMedicalHistoryAndReturnsDTO() {
        String noteContent = "Patient reported improvement of symptoms after adjustment to prescription.";

        PatientRecordUpdateDTO updateDTO = new PatientRecordUpdateDTO();
        updateDTO.setMedicalNote(noteContent);

        MedicalHistory savedHistory = new MedicalHistory();
        savedHistory.setId(999L);
        savedHistory.setPatientId(12345L);
        savedHistory.setDoctorId(777L);
        savedHistory.setDiagnosis(noteContent);
        savedHistory.setStartDate(new java.util.Date());

        when(userRepository.findById(12345L)).thenReturn(Optional.of(existingUser));
        when(allergyRepository.findAllByPatientId(12345L)).thenReturn(List.of());
        when(medicationRepository.findAllByPatientId(12345L)).thenReturn(List.of());
        when(medicalHistoryRepo.findAllByPatientId(12345L)).thenReturn(List.of(savedHistory));
        when(medicalHistoryRepo.save(any(MedicalHistory.class))).thenAnswer(inv -> (MedicalHistory) inv.getArgument(0));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> (User) inv.getArgument(0));

        ResponseEntity<PatientRecordDTO> response = patientRecordService.updatePatientRecord(12345L, updateDTO);

        assertThat(response).isNotNull();
        PatientRecordDTO dto = response.getBody();
        assertThat(dto).isNotNull();
        assertThat(dto.getMedicalHistory()).isNotNull();
        assertThat(dto.getMedicalHistory()).hasSize(1);
        assertThat(dto.getMedicalHistory().get(0).getNotes()).isEqualTo(noteContent);
        verify(medicalHistoryRepo).save(any(MedicalHistory.class)); // invocation checked without nullness enforcement needed
    }
}
