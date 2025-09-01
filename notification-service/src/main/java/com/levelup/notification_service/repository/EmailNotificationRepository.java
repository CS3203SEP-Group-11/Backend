package com.levelup.notification_service.repository;

import com.levelup.notification_service.entity.EmailNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface EmailNotificationRepository extends JpaRepository<EmailNotification, UUID> {
}