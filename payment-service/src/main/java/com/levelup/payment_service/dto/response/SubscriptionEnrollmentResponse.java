package com.levelup.payment_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionEnrollmentResponse {
    private String message;
    private List<UUID> enrolledCourseIds;
    private boolean success;
}