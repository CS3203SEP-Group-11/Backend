package com.levelup.notification_service.repository;

import com.levelup.notification_service.entity.EmailNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface EmailNotificationRepository extends JpaRepository<EmailNotification, UUID> {
    List<EmailNotification> findByRecipientEmail(UUID recipientEmail);
}