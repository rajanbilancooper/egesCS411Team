package com.Eges411Team.UnifiedPatientManager.entity;

import java.time.LocalDateTime;
import java.sql.Blob;

// this will be a class for users in the system
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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
    @Column (name = "content")
    private String content;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @Column (name = "attachment_name")
    private String attachmentName;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "attachment_data")
    private byte[] attachmentData;
}