package com.videotogether.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "rooms")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String securityKey;

    private String mediaSource; // "yt" or "file"

    private String ytId;

    private String hostUsername;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Room() {
    }

    public Room(String name, String securityKey, String mediaSource, String ytId, String hostUsername) {
        this.name = name;
        this.securityKey = securityKey;
        this.mediaSource = mediaSource;
        this.ytId = ytId;
        this.hostUsername = hostUsername;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getSecurityKey() { return securityKey; }
    public void setSecurityKey(String securityKey) { this.securityKey = securityKey; }
    
    public String getMediaSource() { return mediaSource; }
    public void setMediaSource(String mediaSource) { this.mediaSource = mediaSource; }
    
    public String getYtId() { return ytId; }
    public void setYtId(String ytId) { this.ytId = ytId; }
    
    public String getHostUsername() { return hostUsername; }
    public void setHostUsername(String hostUsername) { this.hostUsername = hostUsername; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
