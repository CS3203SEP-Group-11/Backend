package com.levelup.payment_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "pending_purchase_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingPurchaseItem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "stripe_payment_intent_id", nullable = false)
    private String stripePaymentIntentId;

    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Column(name = "course_name", nullable = false)
    private String courseName;

    @Column(name = "course_price", nullable = false)
    private java.math.BigDecimal coursePrice;
}