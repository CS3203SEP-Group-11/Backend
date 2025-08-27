package com.levelup.notification_service.dto;

import com.levelup.notification_service.entity.InAppType;
import java.util.UUID;

public class InAppNotificationRequest {
    private UUID userId;
    private String title;
    private String body;
    private InAppType type;

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public InAppType getType() { return type; }
    public void setType(InAppType type) { this.type = type; }
}
