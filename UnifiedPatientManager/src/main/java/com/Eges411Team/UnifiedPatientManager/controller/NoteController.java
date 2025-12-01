package com.Eges411Team.UnifiedPatientManager.controller;

import com.Eges411Team.UnifiedPatientManager.DTOs.requests.NoteRequestDTO;
import com.Eges411Team.UnifiedPatientManager.entity.Note;
import com.Eges411Team.UnifiedPatientManager.entity.NoteType;
import com.Eges411Team.UnifiedPatientManager.services.NoteService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
        // Basic validation depending on type
        NoteType type = noteRequestDTO.getNoteType();

        // if try to upload nothing
        if (type == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        final long MAX_FILE_BYTES = 5 * 1024 * 1024; // 5MB max size

        // handle TEXT vs FILE note types for null/empty checks
        if (type == NoteType.TEXT) {
            if (noteRequestDTO.getContent() == null || noteRequestDTO.getContent().trim().isEmpty()) {
                throw new IllegalArgumentException("Content is required for TEXT notes");
            }
            if (attachment != null && !attachment.isEmpty()) {
                throw new IllegalArgumentException("FILE attachment provided for TEXT note; remove attachment or change type to FILE");
            }
        } else if (type == NoteType.FILE) {
            if (attachment == null || attachment.isEmpty()) {
                throw new IllegalArgumentException("Attachment file is required for FILE notes");
            }
            if (attachment.getSize() > MAX_FILE_BYTES) {
                throw new IllegalArgumentException("Attachment exceeds 5MB size limit");
            }
        }

        // create a new Note and populate fields with DTO data
        Note note = new Note();
        note.setPatientId(patientId);
        note.setDoctorId(noteRequestDTO.getDoctorId());
        note.setNoteType(type);
        note.setTimestamp(noteRequestDTO.getTimestamp() != null ? noteRequestDTO.getTimestamp() : LocalDateTime.now());

        // set content or attachment based on type
        if (type == NoteType.TEXT) {
            note.setContent(noteRequestDTO.getContent());
        } else if (type == NoteType.FILE) {
            // Store file bytes
            String desiredName = (noteRequestDTO.getAttachmentName() != null && !noteRequestDTO.getAttachmentName().isBlank())
                ? noteRequestDTO.getAttachmentName()
                : attachment.getOriginalFilename();
            note.setAttachmentName(desiredName);
            note.setAttachmentData(attachment.getBytes());
            // Optional: ignore content even if provided
            note.setContent(null);
        }

        // save the note to the repo via service
        Note saved = noteService.saveSingleNote(note);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // Update a specific note
    @PutMapping("/{patient_id}/notes/{note_id}")
    @Operation(summary = "Update a specific note for a patient")
    public ResponseEntity<Note> update(
        @PathVariable("patient_id")
        @Parameter(example = "3")
        Long patientId,
        @PathVariable("note_id")
        @Parameter(example = "1")
        Long noteId,
        @Valid @RequestPart("note") NoteRequestDTO noteRequestDTO,
        @RequestPart(value = "attachment", required = false) MultipartFile attachment
        ) throws IOException {

        // Basic validation depending on type
        NoteType type = noteRequestDTO.getNoteType();
        if (type == null) {
            throw new IllegalArgumentException("Note type required");
        }

        final long MAX_FILE_BYTES = 5 * 1024 * 1024; // 5MB max size

        // handle TEXT vs FILE note types for null/empty checks
        if (type == NoteType.TEXT) {
            if (noteRequestDTO.getContent() == null || noteRequestDTO.getContent().trim().isEmpty()) {
                throw new IllegalArgumentException("Content is required for TEXT notes");
            }
            if (attachment != null && !attachment.isEmpty()) {
                throw new IllegalArgumentException("Attachment provided for TEXT note; change type to FILE to upload");
            }
        } else if (type == NoteType.FILE) {
            if (attachment == null || attachment.isEmpty()) {
                throw new IllegalArgumentException("Attachment file is required for FILE notes");
            }
            if (attachment.getSize() > MAX_FILE_BYTES) {
                throw new IllegalArgumentException("Attachment exceeds 5MB size limit");
            }
        }

        // prepare updated Note object and populate fields with DTO data
        Note updated = new Note();
        updated.setDoctorId(noteRequestDTO.getDoctorId());
        updated.setNoteType(type);
        updated.setTimestamp(noteRequestDTO.getTimestamp());
        if (type == NoteType.TEXT) {
            // set content for TEXT type
            updated.setContent(noteRequestDTO.getContent());
            updated.setAttachmentName(null);
            updated.setAttachmentData(null);
        } else {
            // otherwise clear content for FILE type, and store attachment + its data
            updated.setContent(null);
            String desiredName = (noteRequestDTO.getAttachmentName() != null && !noteRequestDTO.getAttachmentName().isBlank())
                ? noteRequestDTO.getAttachmentName()
                : attachment.getOriginalFilename();
            updated.setAttachmentName(desiredName);
            updated.setAttachmentData(attachment.getBytes());
        }

        // call service to update 
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

    // Download attachment for FILE note
    @GetMapping("/{patient_id}/notes/{note_id}/download")
    @Operation(summary = "Download the attachment for a FILE note")
    public ResponseEntity<byte[]> download(
        @PathVariable("patient_id") Long patientId,
        @PathVariable("note_id") Long noteId
    ) {
        Note note = noteService.getNoteForPatient(patientId, noteId);
        if (note.getNoteType() != NoteType.FILE || note.getAttachmentData() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + (note.getAttachmentName() == null ? "attachment.bin" : note.getAttachmentName()));
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        return new ResponseEntity<>(note.getAttachmentData(), headers, HttpStatus.OK);
    }
}
