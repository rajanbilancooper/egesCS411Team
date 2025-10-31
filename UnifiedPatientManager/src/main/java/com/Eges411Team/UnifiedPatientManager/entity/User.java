package com.Eges411Team.UnifiedPatientManager.entity;

// this will be an abstract class for users in the system
import jakarta.persistence.*;

public abstract class User {
    // fields common to all users
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer id;

    private String username;
    private String password;
    private String role; // e.g., "ADMIN", "DOCTOR", "NURSE"

    // empty constructor required by JPA
    public User() {
        // no params
    }

    // constructor with parameters
    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // getters and setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}