package com.smartdesk.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "activity_logs")
public class ActivityLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String appName;
    private long activeSeconds;
    private LocalDateTime loggedAt = LocalDateTime.now();

    // Getters & setters...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getAppName() { return appName; }
    public void setAppName(String appName) { this.appName = appName; }
    public long getActiveSeconds() { return activeSeconds; }
    public void setActiveSeconds(long activeSeconds) { this.activeSeconds = activeSeconds; }
    public LocalDateTime getLoggedAt() { return loggedAt; }
    public void setLoggedAt(LocalDateTime loggedAt) { this.loggedAt = loggedAt; }
}
