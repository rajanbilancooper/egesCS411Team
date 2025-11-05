package com.Eges411Team.UnifiedPatientManager.entity;

// this will be a class for users in the system
import jakarta.persistence.*;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "medical_history")
@Getter
@Setter

public class MedicalHistory {

    @Id

    @Column(name = "id")
    private Integer id;

    @Column(name = "patient_id")
    private Integer patient_id;

    @Column(name = "doctor_id")
    private Integer doctor_id;

    @Column(name = "diagnosis")
    private String diagnosis;

    @Column(name = "frequency")
    private String frequency;

    @Column(name = "start_date")
    @Temporal(TemporalType.DATE)
    private Date start_date;

    @Column(name = "end_date")
    @Temporal(TemporalType.DATE)
    private Date end_date;

}