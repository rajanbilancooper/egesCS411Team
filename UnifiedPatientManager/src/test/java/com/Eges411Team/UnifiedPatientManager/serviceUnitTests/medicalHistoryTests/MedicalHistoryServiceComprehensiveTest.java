package com.Eges411Team.UnifiedPatientManager.serviceUnitTests.medicalHistoryTests;

import com.Eges411Team.UnifiedPatientManager.entity.MedicalHistory;
import com.Eges411Team.UnifiedPatientManager.repositories.MedicalHistoryRepo;
import com.Eges411Team.UnifiedPatientManager.services.MedicalHistoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for MedicalHistoryService covering all branches
 */
@ExtendWith(MockitoExtension.class)
class MedicalHistoryServiceComprehensiveTest {

    @Mock
    private MedicalHistoryRepo medicalHistoryRepository;

    @InjectMocks
    private MedicalHistoryService medicalHistoryService;

    // ==================== getMedicalHistoryByPatientId ====================
    @Test
    void getMedicalHistoryByPatientId_withHistory_returnsAll() {
        Long patientId = 1L;
        List<MedicalHistory> history = List.of(
            buildMedicalHistory(1L, patientId, "Diabetes"),
            buildMedicalHistory(2L, patientId, "Hypertension")
        );

        when(medicalHistoryRepository.findAllByPatientId(patientId)).thenReturn(history);

        List<MedicalHistory> result = medicalHistoryService.getMedicalHistoryByPatientId(patientId);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(medicalHistoryRepository).findAllByPatientId(patientId);
    }

    @Test
    void getMedicalHistoryByPatientId_noHistory_returnsEmpty() {
        Long patientId = 2L;

        when(medicalHistoryRepository.findAllByPatientId(patientId)).thenReturn(new ArrayList<>());

        List<MedicalHistory> result = medicalHistoryService.getMedicalHistoryByPatientId(patientId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== refreshMedicalHistory ====================
    @Test
    void refreshMedicalHistory_returnsLatestList() {
        Long patientId = 3L;
        List<MedicalHistory> history = List.of(buildMedicalHistory(5L, patientId, "Diabetes"));

        when(medicalHistoryRepository.findAllByPatientId(patientId)).thenReturn(history);

        List<MedicalHistory> result = medicalHistoryService.refreshMedicalHistory(patientId);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(medicalHistoryRepository).findAllByPatientId(patientId);
    }

    // ==================== saveMedicalHistory(Long, List) ====================
    @Test
    void saveMedicalHistory_deletesExistingAndSavesNew() throws Exception {
        Long patientId = 4L;

        MedicalHistory oldHistory = buildMedicalHistory(1L, patientId, "Old diagnosis");
        List<MedicalHistory> existingHistory = List.of(oldHistory);

        MedicalHistory newHistory1 = buildMedicalHistory(null, null, "New diagnosis 1");
        MedicalHistory newHistory2 = buildMedicalHistory(null, null, "New diagnosis 2");
        List<MedicalHistory> newHistoryList = List.of(newHistory1, newHistory2);

        when(medicalHistoryRepository.findAllByPatientId(patientId)).thenReturn(existingHistory);
        when(medicalHistoryRepository.saveAll(anyList())).thenReturn(newHistoryList);

        List<MedicalHistory> result = medicalHistoryService.saveMedicalHistory(patientId, newHistoryList);

        verify(medicalHistoryRepository).deleteAll(existingHistory);
        verify(medicalHistoryRepository).saveAll(anyList());
        assertEquals(2, result.size());
    }

    @Test
    void saveMedicalHistory_emptyNewList_deletesExistingAndSavesNothing() {
        Long patientId = 5L;

        MedicalHistory oldHistory = buildMedicalHistory(1L, patientId, "Old diagnosis");
        List<MedicalHistory> existingHistory = List.of(oldHistory);
        List<MedicalHistory> emptyHistoryList = new ArrayList<>();

        when(medicalHistoryRepository.findAllByPatientId(patientId)).thenReturn(existingHistory);
        when(medicalHistoryRepository.saveAll(anyList())).thenReturn(emptyHistoryList);

        List<MedicalHistory> result = medicalHistoryService.saveMedicalHistory(patientId, emptyHistoryList);

        verify(medicalHistoryRepository).deleteAll(existingHistory);
        assertTrue(result.isEmpty());
    }

    // ==================== updateMedicalHistory ====================
    @Test
    void updateMedicalHistory_notFound_throwsNotFound() {
        Long patientId = 6L;
        Long historyId = 999L;

        when(medicalHistoryRepository.findById(historyId)).thenReturn(Optional.empty());

        MedicalHistory updated = buildMedicalHistory(null, null, "Updated diagnosis");

        assertThrows(ResponseStatusException.class, () ->
            medicalHistoryService.updateMedicalHistory(patientId, historyId, updated)
        );

        verify(medicalHistoryRepository).findById(historyId);
    }

    @Test
    void updateMedicalHistory_wrongPatient_throwsBadRequest() {
        Long patientId = 7L;
        Long wrongPatientId = 99L;
        Long historyId = 10L;

        MedicalHistory existing = buildMedicalHistory(historyId, wrongPatientId, "Existing diagnosis");

        when(medicalHistoryRepository.findById(historyId)).thenReturn(Optional.of(existing));

        MedicalHistory updated = buildMedicalHistory(null, null, "Updated diagnosis");

        assertThrows(ResponseStatusException.class, () ->
            medicalHistoryService.updateMedicalHistory(patientId, historyId, updated)
        );

        verify(medicalHistoryRepository).findById(historyId);
        verify(medicalHistoryRepository, never()).save(any());
    }

    @Test
    void updateMedicalHistory_patientIdNull_throwsBadRequest() {
        Long patientId = 8L;
        Long historyId = 11L;

        MedicalHistory existing = buildMedicalHistory(historyId, null, "Existing diagnosis");

        when(medicalHistoryRepository.findById(historyId)).thenReturn(Optional.of(existing));

        MedicalHistory updated = buildMedicalHistory(null, null, "Updated diagnosis");

        assertThrows(ResponseStatusException.class, () ->
            medicalHistoryService.updateMedicalHistory(patientId, historyId, updated)
        );

        verify(medicalHistoryRepository, never()).save(any());
    }

    @Test
    void updateMedicalHistory_validUpdate_successfullyUpdates() throws Exception {
        Long patientId = 9L;
        Long historyId = 12L;

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        Date startDate = sdf.parse("01/01/2023");
        Date endDate = sdf.parse("12/31/2023");

        MedicalHistory existing = buildMedicalHistory(historyId, patientId, "Original diagnosis");
        existing.setStartDate(startDate);
        existing.setEndDate(endDate);

        MedicalHistory updated = new MedicalHistory();
        updated.setDiagnosis("Updated diagnosis");
        updated.setDoctorId(200L);
        updated.setFrequency("Monthly");
        updated.setStartDate(startDate);
        updated.setEndDate(endDate);

        when(medicalHistoryRepository.findById(historyId)).thenReturn(Optional.of(existing));
        when(medicalHistoryRepository.save(any(MedicalHistory.class))).thenAnswer(inv -> inv.getArgument(0));

        MedicalHistory result = medicalHistoryService.updateMedicalHistory(patientId, historyId, updated);

        assertNotNull(result);
        assertEquals("Updated diagnosis", result.getDiagnosis());
        assertEquals(200L, result.getDoctorId());
        verify(medicalHistoryRepository).save(any());
    }

    // ==================== deleteMedicalHistory ====================
    @Test
    void deleteMedicalHistory_notFound_throwsNotFound() {
        Long patientId = 10L;
        Long historyId = 888L;

        when(medicalHistoryRepository.findById(historyId)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () ->
            medicalHistoryService.deleteMedicalHistory(patientId, historyId)
        );

        verify(medicalHistoryRepository, never()).delete(any());
    }

    @Test
    void deleteMedicalHistory_wrongPatient_throwsBadRequest() {
        Long patientId = 11L;
        Long wrongPatientId = 88L;
        Long historyId = 13L;

        MedicalHistory existing = buildMedicalHistory(historyId, wrongPatientId, "History");

        when(medicalHistoryRepository.findById(historyId)).thenReturn(Optional.of(existing));

        assertThrows(ResponseStatusException.class, () ->
            medicalHistoryService.deleteMedicalHistory(patientId, historyId)
        );

        verify(medicalHistoryRepository, never()).delete(any());
    }

    @Test
    void deleteMedicalHistory_patientIdNull_throwsBadRequest() {
        Long patientId = 12L;
        Long historyId = 14L;

        MedicalHistory existing = buildMedicalHistory(historyId, null, "History");

        when(medicalHistoryRepository.findById(historyId)).thenReturn(Optional.of(existing));

        assertThrows(ResponseStatusException.class, () ->
            medicalHistoryService.deleteMedicalHistory(patientId, historyId)
        );

        verify(medicalHistoryRepository, never()).delete(any());
    }

    @Test
    void deleteMedicalHistory_validHistory_successfullyDeletes() {
        Long patientId = 13L;
        Long historyId = 15L;

        MedicalHistory existing = buildMedicalHistory(historyId, patientId, "History");

        when(medicalHistoryRepository.findById(historyId)).thenReturn(Optional.of(existing));

        medicalHistoryService.deleteMedicalHistory(patientId, historyId);

        verify(medicalHistoryRepository).delete(existing);
    }

    // ==================== saveMedicalHistory(MedicalHistory) - Date Validation ====================
    @Test
    void saveMedicalHistory_bothDatesNull_successfullySaves() {
        MedicalHistory history = buildMedicalHistory(null, 1L, "Diagnosis");
        history.setStartDate(null);
        history.setEndDate(null);

        when(medicalHistoryRepository.save(any(MedicalHistory.class))).thenAnswer(inv -> inv.getArgument(0));

        MedicalHistory result = medicalHistoryService.saveMedicalHistory(history);

        assertNotNull(result);
        assertNull(result.getStartDate());
        assertNull(result.getEndDate());
        verify(medicalHistoryRepository).save(any());
    }

    @Test
    void saveMedicalHistory_onlyStartDateNull_successfullySaves() {
        MedicalHistory history = buildMedicalHistory(null, 1L, "Diagnosis");
        history.setStartDate(null);

        when(medicalHistoryRepository.save(any(MedicalHistory.class))).thenAnswer(inv -> inv.getArgument(0));

        MedicalHistory result = medicalHistoryService.saveMedicalHistory(history);

        assertNotNull(result);
        verify(medicalHistoryRepository).save(any());
    }

    @Test
    void saveMedicalHistory_onlyEndDateNull_successfullySaves() {
        MedicalHistory history = buildMedicalHistory(null, 1L, "Diagnosis");
        history.setEndDate(null);

        when(medicalHistoryRepository.save(any(MedicalHistory.class))).thenAnswer(inv -> inv.getArgument(0));

        MedicalHistory result = medicalHistoryService.saveMedicalHistory(history);

        assertNotNull(result);
        verify(medicalHistoryRepository).save(any());
    }

    @Test
    void saveMedicalHistory_endDateBeforeStartDate_throwsValidationException() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        Date startDate = sdf.parse("12/31/2023");
        Date endDate = sdf.parse("01/01/2023");

        MedicalHistory history = buildMedicalHistory(null, 1L, "Diagnosis");
        history.setStartDate(startDate);
        history.setEndDate(endDate);

        assertThrows(ResponseStatusException.class, () ->
            medicalHistoryService.saveMedicalHistory(history)
        );

        verify(medicalHistoryRepository, never()).save(any());
    }

    @Test
    void saveMedicalHistory_endDateEqualsStartDate_successfullySaves() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        Date sameDate = sdf.parse("01/01/2023");

        MedicalHistory history = buildMedicalHistory(null, 1L, "Diagnosis");
        history.setStartDate(sameDate);
        history.setEndDate(sameDate);

        when(medicalHistoryRepository.save(any(MedicalHistory.class))).thenAnswer(inv -> inv.getArgument(0));

        MedicalHistory result = medicalHistoryService.saveMedicalHistory(history);

        assertNotNull(result);
        verify(medicalHistoryRepository).save(any());
    }

    @Test
    void saveMedicalHistory_endDateAfterStartDate_successfullySaves() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        Date startDate = sdf.parse("01/01/2023");
        Date endDate = sdf.parse("12/31/2023");

        MedicalHistory history = buildMedicalHistory(null, 1L, "Diagnosis");
        history.setStartDate(startDate);
        history.setEndDate(endDate);

        when(medicalHistoryRepository.save(any(MedicalHistory.class))).thenAnswer(inv -> inv.getArgument(0));

        MedicalHistory result = medicalHistoryService.saveMedicalHistory(history);

        assertNotNull(result);
        verify(medicalHistoryRepository).save(any());
    }

    @Test
    void saveMedicalHistory_startDateWithoutEndDate_successfullySaves() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        Date startDate = sdf.parse("01/01/2023");

        MedicalHistory history = buildMedicalHistory(null, 1L, "Diagnosis");
        history.setStartDate(startDate);
        history.setEndDate(null);  // Only start date present

        when(medicalHistoryRepository.save(any(MedicalHistory.class))).thenAnswer(inv -> inv.getArgument(0));

        MedicalHistory result = medicalHistoryService.saveMedicalHistory(history);

        assertNotNull(result);
        assertEquals(startDate, result.getStartDate());
        assertNull(result.getEndDate());
        verify(medicalHistoryRepository).save(any());
    }

    @Test
    void saveMedicalHistory_endDateWithoutStartDate_successfullySaves() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        Date endDate = sdf.parse("12/31/2023");

        MedicalHistory history = buildMedicalHistory(null, 1L, "Diagnosis");
        history.setStartDate(null);  // Only end date present
        history.setEndDate(endDate);

        when(medicalHistoryRepository.save(any(MedicalHistory.class))).thenAnswer(inv -> inv.getArgument(0));

        MedicalHistory result = medicalHistoryService.saveMedicalHistory(history);

        assertNotNull(result);
        assertNull(result.getStartDate());
        assertEquals(endDate, result.getEndDate());
        verify(medicalHistoryRepository).save(any());
    }

    // ==================== Helper Method ====================
    private MedicalHistory buildMedicalHistory(Long id, Long patientId, String diagnosis) {
        MedicalHistory history = new MedicalHistory();
        history.setId(id);
        history.setPatientId(patientId);
        history.setDoctorId(100L);
        history.setDiagnosis(diagnosis);
        history.setFrequency("Monthly");
        return history;
    }
}
