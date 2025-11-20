package com.Eges411Team.UnifiedPatientManager.entity;

// this will be a class for users in the system
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/** @author emilygoyal */
@Entity
@Table(name = "medication")
@Getter
@Setter

public class Medication{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "patient_id")
    private Long patientId;

    @Column(name = "doctor_id")
    private Long doctorId;

    @Column (name = "drug_name")
    private String drugName;

    @Column (name = "dose")
    private String dose;

    @Column (name = "frequency")
    private String frequency;

    @Column (name = "duration")
    private String duration;

    @Column (name = "notes")
    private String notes;

    // Route of administration (oral, IV, IM, topical, etc.)
    @Column(name = "route", length = 40)
    private String route;

    @Column (name = "timestamp")
    private LocalDateTime timestamp;

    @Column (name = "status")
    private Boolean status;

    @Column (name = "is_perscription")
    private Boolean isPerscription;

    // Conflict tracking (simple audit of prescription decision)
    @Column(name = "conflict_flag")
    private Boolean conflictFlag; // true if conflicts were detected when added

    @Lob
    @Column(name = "conflict_details")
    private String conflictDetails; // concatenated conflict messages

    @Lob
    @Column(name = "override_justification")
    private String overrideJustification; // doctor's reason for override
}