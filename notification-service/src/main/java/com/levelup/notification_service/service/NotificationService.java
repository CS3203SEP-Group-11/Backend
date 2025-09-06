package com.levelup.notification_service.service;

import com.levelup.notification_service.entity.*;
import com.levelup.notification_service.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.UUID;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private EmailNotificationRepository emailNotificationRepository;
    @Autowired
    private InAppNotificationRepository inAppNotificationRepository;

    public Notification createNotification(String userId, NotificationType type, String content) {
        Notification notification = new Notification();
        notification.setId(UUID.randomUUID());
        notification.setUserId(userId);
        notification.setType(type);
        notification.setContent(content);
        notification.setStatus(NotificationStatus.PENDING);
        notification.setCreatedAt(Instant.now());
        notification.setUpdatedAt(Instant.now());
        return notificationRepository.save(notification);
    }

    public EmailNotification createEmailNotification(Notification notification, String subject, String body, String recipientEmail) {
        EmailNotification emailNotification = new EmailNotification();
        emailNotification.setId(notification.getId());
        emailNotification.setSubject(subject);
        emailNotification.setBody(body);
        emailNotification.setRecipientEmail(recipientEmail);
        emailNotification.setRetryCount(0);
        return emailNotificationRepository.save(emailNotification);
    }

    public InAppNotification createInAppNotification(Notification notification, String title, InAppType type, String body) {
        InAppNotification inAppNotification = new InAppNotification();
        inAppNotification.setId(notification.getId());
        inAppNotification.setTitle(title);
        inAppNotification.setType(type);
        inAppNotification.setBody(body);
        inAppNotification.setRead(false);
        return inAppNotificationRepository.save(inAppNotification);
    }

    public Notification updateNotificationStatus(UUID notificationId, NotificationStatus status) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setStatus(status);
        if (status == NotificationStatus.SENT) {
            notification.setSentAt(Instant.now());
        }
        notification.setUpdatedAt(Instant.now());
        return notificationRepository.save(notification);
    }

    public NotificationRepository getNotificationRepository() {
        return notificationRepository;
    }

    public List<Notification> getNotificationsByUserId(String userId) {
        return notificationRepository.findByUserId(userId);
    }

    public Optional<Notification> getNotificationById(UUID id) {
        return notificationRepository.findById(id);
    }

    public List<Notification> getNotificationsByStatus(NotificationStatus status) {
        return notificationRepository.findByStatus(status);
    }
}