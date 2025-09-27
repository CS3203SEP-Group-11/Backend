package com.levelup.payment_service.model;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "subscription_plans")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_cycle", length = 20, nullable = false)
    private BillingCycle billingCycle;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Type(JsonType.class)
    @Column(name = "features", columnDefinition = "jsonb")
    private Map<String, Object> features;

    @Column(name = "stripe_price_id", length = 255)
    private String stripePriceId;

    public enum BillingCycle {
        MONTHLY,
        ANNUAL
    }
}