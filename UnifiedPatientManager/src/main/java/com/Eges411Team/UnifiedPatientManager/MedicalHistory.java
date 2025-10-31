package com.Eges411Team.UnifiedPatientManager;

import jakarta.persistence.*;

@Entity
@Table(name = "medical_history")
public class MedicalHistory {

    // empty constructor required by JPA
    public MedicalHistory() {
        // no params
    }
}