package com.Eges411Team.UnifiedPatientManager.entity;

import jakarta.persistence.*;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "audit_log")
@Getter
@Setter

public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "log_id")
    private Integer log_id;

    @Column(name = "user_id")
    private Integer user_id;

    @Column(name = "doctor_id")
    private Integer doctor_id;

    @Column(name = "performed_by")
    private String performedBy;

    @Column(name = "timestamp")
    private Date timestamp;

    @Column(name = "field_changed")
    private String field_changed;

    @Column(name = "change_made")
    private String change_made;


    public AuditLog() {
        // no params
    }

    public AuditLog(String change, Date timestamp, String performedBy) {
        this.change_made = change;
        this.timestamp = timestamp;
        this.performedBy = performedBy;
    }
}
