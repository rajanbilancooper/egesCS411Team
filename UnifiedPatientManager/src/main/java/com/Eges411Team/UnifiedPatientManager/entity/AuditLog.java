package com.Eges411Team.UnifiedPatientManager.entity;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Integer id;
    private String action;
    private Date timestamp;
    private String performedBy;

    public AuditLog() {
        // no params
    }

    public AuditLog(String action, Date timestamp, String performedBy) {
        this.action = action;
        this.timestamp = timestamp;
        this.performedBy = performedBy;
    }

}
