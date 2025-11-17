package com.Eges411Team.UnifiedPatientManager.entity;

// this will be a class for users in the system
import jakarta.persistence.*;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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

    @Column (name = "timestamp")
    private LocalDateTime timestamp;

    @Column (name = "status")
    private Boolean status;

    @Column (name = "is_perscription")
    private Boolean isPerscription;
}