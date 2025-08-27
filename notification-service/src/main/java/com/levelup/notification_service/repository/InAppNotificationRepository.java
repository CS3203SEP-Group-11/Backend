package com.levelup.notification_service.repository;

import com.levelup.notification_service.entity.InAppNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface InAppNotificationRepository extends JpaRepository<InAppNotification, UUID> {
}