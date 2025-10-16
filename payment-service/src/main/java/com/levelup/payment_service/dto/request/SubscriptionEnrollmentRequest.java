package com.levelup.payment_service.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionEnrollmentRequest {

    @NotNull(message = "Course IDs are required")
    @NotEmpty(message = "At least one course ID must be provided")
    private List<UUID> courseIds;
}