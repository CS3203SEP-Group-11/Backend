package com.levelup.payment_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_subscription_payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSubscriptionPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;

    @OneToOne
    @JoinColumn(name = "transaction_id", unique = true, nullable = false)
    private Transaction transaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_plan_id", nullable = false)
    private SubscriptionPlan subscriptionPlan;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "stripe_subscription_id", length = 255)
    private String stripeSubscriptionId;

    @Column(name = "stripe_invoice_id", length = 255)
    private String stripeInvoiceId;

    @Column(name = "stripe_refund_id", length = 255)
    private String stripeRefundId;

    @Column(name = "first_period_start")
    private LocalDateTime firstPeriodStart;

    @Column(name = "first_period_end")
    private LocalDateTime firstPeriodEnd;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    @Column(name = "after_renewal_end")
    private LocalDateTime afterRenewalEnd;

    @Column(name = "is_auto_renew", nullable = false)
    @Builder.Default
    private Boolean isAutoRenew = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private SubscriptionStatus status;

    public enum SubscriptionStatus {
        ACTIVE,
        CANCELED,
        REFUNDED
    }

    public java.time.LocalDateTime getCreatedAt() {
        // Use firstPeriodStart as the creation time for analytics
        return this.firstPeriodStart;
    }
}