package com.Eges411Team.UnifiedPatientManager.entity;

import java.sql.Blob;
import java.time.LocalDateTime;

// this will be a class for users in the system
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


/** @author emilygoyal */
@Entity
@Table(name = "note")
@Getter
@Setter

public class Note{
    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "patient_id")
    private int patient_id;

    @Column(name = "doctor_id")
    private int doctor_id;

    @Column (name = "note_type")
    private Blob note_type;

    @Column (name = "content")
    private Blob content;

    @Column (name = "timestamp")
    private LocalDateTime timestamp;
}