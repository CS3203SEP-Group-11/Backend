package com.levelup.payment_service.dto.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentNotificationMessage {
    private UUID userId;
    private String eventType; // PURCHASE_SUCCESS, PURCHASE_FAILED
    private List<String> courseNames;
    private String amount;
    private String currency;
}