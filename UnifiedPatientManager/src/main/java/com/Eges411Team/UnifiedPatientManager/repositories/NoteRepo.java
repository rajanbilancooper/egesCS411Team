package com.Eges411Team.UnifiedPatientManager.repositories;

import com.Eges411Team.UnifiedPatientManager.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NoteRepo extends JpaRepository<Note, Integer> {

    // Get all notes for a given patient
    List<Note> findAllByPatient_id(int patient_id);

    // Get all notes written by a specific doctor
    List<Note> findAllByDoctor_id(int doctor_id);
}
