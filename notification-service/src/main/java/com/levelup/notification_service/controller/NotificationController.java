package com.levelup.notification_service.controller;

import com.levelup.notification_service.entity.*;
import com.levelup.notification_service.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
import com.levelup.notification_service.dto.EmailNotificationRequest;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;

    @PostMapping("/email")
    public Notification createEmailNotification(@RequestBody EmailNotificationRequest request) {
        Notification notification = notificationService.createNotification(request.getUserId(), NotificationType.EMAIL, request.getBody());
        notificationService.createEmailNotification(notification, request.getSubject(), request.getBody(), request.getUserId());
        return notification;
    }

    @PostMapping("/in-app")
    public Notification createInAppNotification(@RequestParam UUID userId, @RequestParam String title, @RequestParam String body, @RequestParam InAppType type) {
        Notification notification = notificationService.createNotification(userId, NotificationType.IN_APP, body);
        notificationService.createInAppNotification(notification, title, type, body);
        return notification;
    }

    // Additional endpoints for retrieving, updating notifications can be added here
}