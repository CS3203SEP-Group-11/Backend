package com.levelup.notification_service.repository;

import com.levelup.notification_service.entity.Notification;
import com.levelup.notification_service.entity.NotificationStatus;
import com.levelup.notification_service.entity.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByUserId(String userId);
    List<Notification> findByType(NotificationType type);
    List<Notification> findByStatus(NotificationStatus status);
}