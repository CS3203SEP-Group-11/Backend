package com.levelup.payment_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "renewals")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Renewal {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;

    @OneToOne
    @JoinColumn(name = "transaction_id", unique = true, nullable = false)
    private Transaction transaction;

    @Column(name = "stripe_subscription_id", length = 255)
    private String stripeSubscriptionId;

    @Column(name = "stripe_invoice_id", length = 255)
    private String stripeInvoiceId;


    @Column(name = "retry_count", nullable = false)
    @Builder.Default
    private Integer retryCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private RenewalStatus status;

    @Column(name = "next_payment_attempt_at")
    private LocalDateTime nextPaymentAttemptAt;

    @CreationTimestamp
    @Column(name = "renewal_date", nullable = false)
    private LocalDateTime renewalDate;

    public enum RenewalStatus {
        SUCCESS,
        FAILED
    }
}