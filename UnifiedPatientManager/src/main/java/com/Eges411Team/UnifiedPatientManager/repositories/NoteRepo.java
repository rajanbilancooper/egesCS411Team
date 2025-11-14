package com.Eges411Team.UnifiedPatientManager.repositories;

import com.Eges411Team.UnifiedPatientManager.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NoteRepo extends JpaRepository<Note, Long> {

    // Get all notes for a given patient
    List<Note> findAllByPatientId(Long patientId);

    // Get all notes written by a specific doctor
    List<Note> findAllByDoctorId(Long doctorId);
}
