package com.levelup.notification_service.repository;

import com.levelup.notification_service.entity.InAppNotification;
import com.levelup.notification_service.entity.InAppType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface InAppNotificationRepository extends JpaRepository<InAppNotification, UUID> {
    List<InAppNotification> findByType(InAppType type);
    List<InAppNotification> findByRead(boolean read);
}