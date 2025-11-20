package com.Eges411Team.UnifiedPatientManager.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


/** @author emilygoyal */
@Entity
@Table(name = "note")
@Getter
@Setter
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "patient_id")
    private Long patientId;

    @Column(name = "doctor_id")
    private Long doctorId;

    @Lob
    @Column(name = "note_type")
    private String noteType;

    @Lob
    @Column(name = "content")
    private String content;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;
}