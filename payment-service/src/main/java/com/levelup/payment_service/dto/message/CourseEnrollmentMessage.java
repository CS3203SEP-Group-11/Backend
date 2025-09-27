package com.levelup.payment_service.dto.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseEnrollmentMessage {
    private UUID userId;
    private List<UUID> courseIds;
}