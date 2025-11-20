package com.Eges411Team.UnifiedPatientManager.entity;

// this will be a class for users in the system PUSHING NOW 
import jakarta.persistence.*;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "medical_history") // matches name in db
@Getter
@Setter

public class MedicalHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "patient_id")
    private Long patientId;

    @Column(name = "doctor_id")
    private Long doctorId;

    @Column(name = "diagnosis")
    private String diagnosis;

    @Column(name = "frequency")
    private String frequency;

    @Column(name = "start_date")
    @Temporal(TemporalType.DATE)
    private Date startDate;

    @Column(name = "end_date")
    @Temporal(TemporalType.DATE)
    private Date endDate;

}