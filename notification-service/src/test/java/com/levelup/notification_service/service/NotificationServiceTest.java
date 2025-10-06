package com.levelup.notification_service.service;

import com.levelup.notification_service.entity.*;
import com.levelup.notification_service.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private EmailNotificationRepository emailNotificationRepository;

    @Mock
    private InAppNotificationRepository inAppNotificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void createNotification_returnsNotification_whenValidInput() {
        // Given
        UUID userIdUuid = UUID.randomUUID();
        String userId = userIdUuid.toString();
        String content = "Test notification";
        Notification expectedNotification = new Notification();
        expectedNotification.setId(UUID.randomUUID());
        expectedNotification.setUserId(userId);
        expectedNotification.setType(NotificationType.EMAIL);
        expectedNotification.setContent(content);
        expectedNotification.setStatus(NotificationStatus.PENDING);

        when(notificationRepository.save(any(Notification.class))).thenReturn(expectedNotification);

        // When
        Notification result = notificationService.createNotification(userId, NotificationType.EMAIL, content);

        // Then
        assertEquals(userId, result.getUserId());
        assertEquals(NotificationType.EMAIL, result.getType());
        assertEquals(content, result.getContent());
        assertEquals(NotificationStatus.PENDING, result.getStatus());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void createEmailNotification_returnsEmailNotification_whenValidInput() {
        // Given
        Notification notification = new Notification();
        notification.setId(UUID.randomUUID());
        String subject = "Test Subject";
        String body = "Test Body";
        String recipientEmail = "test@example.com";

        EmailNotification expectedEmailNotification = new EmailNotification();
        expectedEmailNotification.setId(notification.getId());
        expectedEmailNotification.setSubject(subject);
        expectedEmailNotification.setBody(body);
        expectedEmailNotification.setRecipientEmail(recipientEmail);

        when(emailNotificationRepository.save(any(EmailNotification.class))).thenReturn(expectedEmailNotification);

        // When
        EmailNotification result = notificationService.createEmailNotification(notification, subject, body,
                recipientEmail);

        // Then
        assertEquals(notification.getId(), result.getId());
        assertEquals(subject, result.getSubject());
        assertEquals(body, result.getBody());
        assertEquals(recipientEmail, result.getRecipientEmail());
        verify(emailNotificationRepository, times(1)).save(any(EmailNotification.class));
    }

    @Test
    void createInAppNotification_returnsInAppNotification_whenValidInput() {
        // Given
        Notification notification = new Notification();
        notification.setId(UUID.randomUUID());
        String title = "Test Title";
        InAppType type = InAppType.INFO;
        String body = "Test Body";

        InAppNotification expectedInAppNotification = new InAppNotification();
        expectedInAppNotification.setId(notification.getId());
        expectedInAppNotification.setTitle(title);
        expectedInAppNotification.setType(type);
        expectedInAppNotification.setBody(body);
        expectedInAppNotification.setRead(false);

        when(inAppNotificationRepository.save(any(InAppNotification.class))).thenReturn(expectedInAppNotification);

        // When
        InAppNotification result = notificationService.createInAppNotification(notification, title, type, body);

        // Then
        assertEquals(notification.getId(), result.getId());
        assertEquals(title, result.getTitle());
        assertEquals(type, result.getType());
        assertEquals(body, result.getBody());
        assertFalse(result.isRead());
        verify(inAppNotificationRepository, times(1)).save(any(InAppNotification.class));
    }
}
