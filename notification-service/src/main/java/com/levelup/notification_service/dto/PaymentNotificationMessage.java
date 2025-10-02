package com.levelup.notification_service.dto;

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
    private String eventType; // PURCHASE_SUCCESS, PURCHASE_FAILED, SUBSCRIPTION_SUCCESS, SUBSCRIPTION_FAILED
    private List<String> courseNames; // For course purchases
    private String subscriptionName; // For subscription purchases
    private String amount;
    private String currency;
    private String invoicePdfUrl; // For subscription invoices
}