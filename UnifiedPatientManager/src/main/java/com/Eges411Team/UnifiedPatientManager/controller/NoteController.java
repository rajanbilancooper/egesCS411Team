package com.Eges411Team.UnifiedPatientManager.controller;

import com.Eges411Team.UnifiedPatientManager.DTOs.requests.NoteRequestDTO;
import com.Eges411Team.UnifiedPatientManager.entity.Note;
import com.Eges411Team.UnifiedPatientManager.services.NoteService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping({"/default/patient", "/api/patients"})
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    // Get all notes for a patient
    @GetMapping("/{patient_id}/notes")
    @Operation(
        summary = "Get a patient's notes",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "List of notes for the given patient",
                content = @Content(
                    mediaType = "application/json",
                    examples = {
                        @ExampleObject(
                            name = "NoteList",
                            summary = "Retrieves a patient's notes.",
                            description = "Retrieves all notes for a given patient.",
                            value = "[{\"id\":1,\"patient_id\":3,\"doctor_id\":5,\"note_type\":\"<BLOB>\",\"content\":\"<BLOB>\",\"timestamp\":\"2025-11-10T09:30:00\"}]"
                        )
                    }
                )
            )
        }
    )
    public ResponseEntity<List<Note>> find(
        @PathVariable("patient_id")
        @Parameter(example = "3")
        Long patientId
    ) {
        List<Note> notes = noteService.getNotesByPatientId(patientId);
        return ResponseEntity.ok(notes);
    }

    // Create or replace patient's notes

    @PostMapping("/{patient_id}/notes")
    @Operation(summary = "Create a new note for a patient")
    public ResponseEntity<Note> createNote(
        @PathVariable("patient_id")
        @Parameter(example = "3") Long patientId,
        @Valid @RequestPart("note") NoteRequestDTO noteRequestDTO,
        @RequestPart(value = "attachment", required = false) MultipartFile attachment
    ) throws IOException {

        Note note = new Note();
        note.setPatientId(patientId);
        note.setDoctorId(noteRequestDTO.getDoctorId());
        note.setNoteType(noteRequestDTO.getNoteType());
        note.setContent(noteRequestDTO.getContent());
        note.setTimestamp(noteRequestDTO.getTimestamp() != null ? noteRequestDTO.getTimestamp() : LocalDateTime.now());

    if (attachment != null && !attachment.isEmpty()) {
        note.setAttachmentName(attachment.getOriginalFilename());
        note.setAttachmentData(attachment.getBytes());
    }

    // Save using the service
    Note saved = noteService.saveSingleNote(note);
    return ResponseEntity.status(HttpStatus.CREATED).body(saved);
}

    // Update a specific note
    @PutMapping("/{patient_id}/notes/{note_id}")
    @Operation(summary = "Update a specific note for a patient")
    public ResponseEntity<Note> update(
        @RequestBody Note note,
        @PathVariable("patient_id")
        @Parameter(example = "3")
        Long patientId,
        @PathVariable("note_id")
        @Parameter(example = "1")
        Long noteId,
        @Valid @RequestPart("note") NoteRequestDTO noteRequestDTO,
        @RequestPart(value = "attachment", required = false) MultipartFile attachment
        ) throws IOException {

        Note updated = new Note();
        updated.setDoctorId(noteRequestDTO.getDoctor_id());
        updated.setNoteType(noteRequestDTO.getNote_type());
        updated.setContent(noteRequestDTO.getContent());
        updated.setTimestamp(noteRequestDTO.getTimestamp());

        if (attachment != null && !attachment.isEmpty()) {
            updated.setAttachmentName(attachment.getOriginalFilename());
            updated.setAttachmentData(attachment.getBytes());
        }

    Note saved = noteService.updateNote(patientId, noteId, updated);
    return ResponseEntity.ok(saved);
    }

    // Refresh patient notes
    @GetMapping("/{patient_id}/notes/refresh")
    @Operation(summary = "Refresh a patient's notes")
    public ResponseEntity<List<Note>> refresh(
        @PathVariable("patient_id")
        @Parameter(example = "3")
        Long patientId
    ) {
        List<Note> refreshed = noteService.refreshNotes(patientId);
        return ResponseEntity.ok(refreshed);
    }

    // Delete a specific note
    @DeleteMapping("/{patient_id}/notes/{note_id}")
    @Operation(summary = "Delete a specific note for a patient")
    public ResponseEntity<HttpStatus> delete(
        @PathVariable("patient_id")
        @Parameter(example = "3")
        Long patientId,
        @PathVariable("note_id")
        @Parameter(example = "1")
        Long noteId
    ) {
        noteService.deleteNote(patientId, noteId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
