package com.Eges411Team.UnifiedPatientManager.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_session")
public class UserSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "login_time")
    private LocalDateTime loginTime;

    @Column(name = "session_token", nullable = false, unique = true)
    private String sessionToken;

    @Column(name = "logout_time")
    private LocalDateTime logoutTime;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "last_activity_time")
    private LocalDateTime lastActivityTime;

    //Contructors 
    public UserSession() {
        //Default Constructor 
    }
    public UserSession(User user, LocalDateTime loginTime) {
        this.user = user;
        this.loginTime = loginTime;
        this.active = true;
    }
    //Business logic methods
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    // used by Spring Data and logical checks
    public boolean isActive() {
        return active && !isExpired();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public String getSessionToken() {
        return sessionToken;
    }   
    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }
    
    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(LocalDateTime loginTime) {
        this.loginTime = loginTime;
    }

    public LocalDateTime getLogoutTime() {
        return logoutTime;
    }

    public void setLogoutTime(LocalDateTime logoutTime) {
        this.logoutTime = logoutTime;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    // keep old-style getter/setter names used by callers
    public boolean getIsActive() {
        return active;
    }
    public void setIsActive(boolean isActive) {
        this.active = isActive;
    }
    public String getIpAddress() {
        return ipAddress;
    }
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;}
    public LocalDateTime getLastActivityTime() {
        return lastActivityTime;
    }
    public void setLastActivityTime(LocalDateTime lastActivityTime) {
        this.lastActivityTime = lastActivityTime;
    }
}

