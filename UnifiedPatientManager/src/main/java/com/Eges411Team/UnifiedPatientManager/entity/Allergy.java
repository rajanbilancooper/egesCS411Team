package com.Eges411Team.UnifiedPatientManager.entity;

// this will be a class for users in the system
import jakarta.persistence.*;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

/** @author emilygoyal */
@Entity
@Table(name = "allergy")
@Getter
@Setter

public class Allergy{
    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "patient_id")
    private int patient_id;

    @Column (name = "reaction")
    private String reaction;

    @Column (name = "severity")
    private String severity;

    @Column (name = "substance")
    private String substance;
}