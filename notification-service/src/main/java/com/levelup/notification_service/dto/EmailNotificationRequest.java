package com.levelup.notification_service.dto;

import java.util.UUID;

public class EmailNotificationRequest {
    private UUID userId;
    private String subject;
    private String body;

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
}
