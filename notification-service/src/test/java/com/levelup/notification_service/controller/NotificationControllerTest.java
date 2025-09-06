package com.levelup.notification_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.levelup.notification_service.dto.EmailNotificationRequest;
import com.levelup.notification_service.entity.*;
import com.levelup.notification_service.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createEmailNotification_returnsNotification_whenValidRequest() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        EmailNotificationRequest request = new EmailNotificationRequest();
        request.setUserId(userId);
        request.setSubject("Test Subject");
        request.setBody("Test Body");

        Notification expectedNotification = new Notification();
        expectedNotification.setId(UUID.randomUUID());
        expectedNotification.setUserId(userId);
        expectedNotification.setType(NotificationType.EMAIL);
        expectedNotification.setContent("Test Body");
        expectedNotification.setStatus(NotificationStatus.PENDING);

        EmailNotification expectedEmailNotification = new EmailNotification();
        expectedEmailNotification.setId(expectedNotification.getId());
        expectedEmailNotification.setSubject("Test Subject");
        expectedEmailNotification.setBody("Test Body");
        expectedEmailNotification.setRecipientEmail(userId);

        when(notificationService.createNotification(eq(userId), eq(NotificationType.EMAIL), eq("Test Body")))
                .thenReturn(expectedNotification);
        when(notificationService.createEmailNotification(any(Notification.class), eq("Test Subject"), eq("Test Body"), eq(userId)))
                .thenReturn(expectedEmailNotification);

        // When & Then
        mockMvc.perform(post("/api/notifications/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.type").value("EMAIL"))
                .andExpect(jsonPath("$.content").value("Test Body"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void createInAppNotification_returnsNotification_whenValidRequest() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        Notification expectedNotification = new Notification();
        expectedNotification.setId(UUID.randomUUID());
        expectedNotification.setUserId(userId);
        expectedNotification.setType(NotificationType.IN_APP);
        expectedNotification.setContent("Test Body");
        expectedNotification.setStatus(NotificationStatus.PENDING);

        InAppNotification expectedInAppNotification = new InAppNotification();
        expectedInAppNotification.setId(expectedNotification.getId());
        expectedInAppNotification.setTitle("Test Title");
        expectedInAppNotification.setType(InAppType.INFO);
        expectedInAppNotification.setBody("Test Body");
        expectedInAppNotification.setRead(false);

        when(notificationService.createNotification(eq(userId), eq(NotificationType.IN_APP), eq("Test Body")))
                .thenReturn(expectedNotification);
        when(notificationService.createInAppNotification(any(Notification.class), eq("Test Title"), eq(InAppType.INFO), eq("Test Body")))
                .thenReturn(expectedInAppNotification);

        // When & Then
        mockMvc.perform(post("/api/notifications/in-app")
                        .param("userId", userId.toString())
                        .param("title", "Test Title")
                        .param("body", "Test Body")
                        .param("type", "INFO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.type").value("IN_APP"))
                .andExpect(jsonPath("$.content").value("Test Body"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }
}
