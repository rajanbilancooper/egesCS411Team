package com.Eges411Team.UnifiedPatientManager.entity;

import jakarta.persistence.*;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "audit_logs")
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

    @Column(name = "timestamp")
    private DateTime timestamp;

    @Column(name = "field_changed")
    private String field_changed;

    @Column(name = "change_made")
    private String change_made;


    public AuditLog() {
        // no params
    }

    public AuditLog(String action, Date timestamp, String performedBy) {
        this.action = action;
        this.timestamp = timestamp;
        this.performedBy = performedBy;
    }

}
