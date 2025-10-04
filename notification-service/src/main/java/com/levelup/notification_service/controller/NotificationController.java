package com.levelup.notification_service.controller;

import com.levelup.notification_service.entity.*;
import com.levelup.notification_service.service.NotificationService;
import com.levelup.notification_service.service.EmailNotificationService;
import com.levelup.notification_service.client.UserServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.List;
import java.util.UUID;
import com.levelup.notification_service.dto.EmailNotificationRequest;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {
    
    private final NotificationService notificationService;
    private final EmailNotificationService emailNotificationService;
    private final UserServiceClient userServiceClient;

    @PostMapping("/email")
    public ResponseEntity<Notification> createEmailNotification(@RequestBody EmailNotificationRequest request) {
        try {
            Notification notification = notificationService.createNotification(
                    request.getUserId(), NotificationType.EMAIL, request.getBody());

            // Fetch user's email from user-service via API Gateway
            String userEmail = getUserEmailById(request.getUserId());

            boolean sent = emailNotificationService.sendEmail(
                    userEmail,
                    request.getSubject(),
                    request.getBody()
            );

            if (!sent) {
                // Update notification status to failed
                notificationService.updateNotificationStatus(notification.getId(), NotificationStatus.FAILED);
                throw new RuntimeException("Failed to send email to: " + userEmail);
            }

            // Update notification status to sent
            notificationService.updateNotificationStatus(notification.getId(), NotificationStatus.SENT);

            notificationService.createEmailNotification(notification, request.getSubject(), request.getBody(), userEmail);
            log.info("Email notification sent successfully to user: {}", request.getUserId());
            return ResponseEntity.ok(notification);
        } catch (RuntimeException e) {
            // Log the error and re-throw with more context
            log.error("Error creating email notification for user {}: {}", request.getUserId(), e.getMessage());
            throw new RuntimeException("Failed to create email notification: " + e.getMessage(), e);
        }
    }

    @PostMapping("/in-app")
    public ResponseEntity<Notification> createInAppNotification(@RequestParam String userId, @RequestParam String title, @RequestParam String body, @RequestParam InAppType type) {
        log.info("Creating in-app notification for user: {}", userId);
        Notification notification = notificationService.createNotification(userId, NotificationType.IN_APP, body);
        notificationService.createInAppNotification(notification, title, type, body);
        return ResponseEntity.ok(notification);
    }

    // Helper method to fetch user email from user-service via Feign Client
    private String getUserEmailById(String userId) {
        try {
            Map<String, Object> userResponse = userServiceClient.getUserById(userId);
            
            if (userResponse == null || userResponse.get("email") == null) {
                throw new RuntimeException("User not found or email not available for user ID: " + userId);
            }
            
            String email = (String) userResponse.get("email");
            if (email == null || email.trim().isEmpty()) {
                throw new RuntimeException("Email is empty for user ID: " + userId);
            }
            
            return email;
        } catch (Exception e) {
            log.error("Failed to fetch user with ID: {}", userId, e);
            throw new RuntimeException("Failed to fetch user with ID: " + userId, e);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notification>> getNotificationsByUserId(@PathVariable String userId) {
        log.info("Fetching notifications for user: {}", userId);
        List<Notification> notifications = notificationService.getNotificationsByUserId(userId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Notification> getNotificationById(@PathVariable UUID id) {
        log.info("Fetching notification with ID: {}", id);
        return notificationService.getNotificationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Notification>> getNotificationsByStatus(@PathVariable NotificationStatus status) {
        log.info("Fetching notifications with status: {}", status);
        List<Notification> notifications = notificationService.getNotificationsByStatus(status);
        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/in-app/{notificationId}/mark-read")
    public ResponseEntity<InAppNotification> markInAppNotificationAsRead(@PathVariable UUID notificationId) {
        log.info("Marking in-app notification as read: {}", notificationId);
        try {
            InAppNotification notification = notificationService.markInAppNotificationAsRead(notificationId);
            return ResponseEntity.ok(notification);
        } catch (RuntimeException e) {
            log.error("Error marking notification as read: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/in-app/user/{userId}")
    public ResponseEntity<List<InAppNotification>> getInAppNotificationsByUserId(@PathVariable String userId) {
        log.info("Fetching in-app notifications for user: {}", userId);
        List<InAppNotification> notifications = notificationService.getInAppNotificationsByUserId(userId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/in-app/{notificationId}")
    public ResponseEntity<InAppNotification> getInAppNotificationById(@PathVariable UUID notificationId) {
        log.info("Fetching in-app notification by ID: {}", notificationId);
        return notificationService.getInAppNotificationById(notificationId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}