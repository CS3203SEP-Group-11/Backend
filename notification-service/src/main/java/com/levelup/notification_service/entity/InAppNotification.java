package com.levelup.notification_service.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "in_app_notifications")
public class InAppNotification {
    @Id
    private UUID id;

    private String title;
    @Enumerated(EnumType.STRING)
    private InAppType type;
    private String body;
    private boolean read;
    private Instant readAt;

    // Getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
    public Instant getReadAt() { return readAt; }
    public void setReadAt(Instant readAt) { this.readAt = readAt; }
    public InAppType getType() { return type; }
    public void setType(InAppType type) { this.type = type; }
}