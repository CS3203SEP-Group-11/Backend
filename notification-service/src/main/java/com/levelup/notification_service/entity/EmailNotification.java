package com.levelup.notification_service.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "email_notification")
public class EmailNotification {
    @Id
    private UUID id;

    private String subject;
    @Column(columnDefinition = "TEXT")
    private String body;

    private String recipientEmail;
    @Column(columnDefinition = "TEXT")
    private String deliveryResponse;
    private int retryCount;

    // Getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public String getRecipientEmail() { return recipientEmail; }
    public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }
    public String getDeliveryResponse() { return deliveryResponse; }
    public void setDeliveryResponse(String deliveryResponse) { this.deliveryResponse = deliveryResponse; }
    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
}