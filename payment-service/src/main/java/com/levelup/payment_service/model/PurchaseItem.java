package com.levelup.payment_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "purchase_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseItem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userpurchase_id", nullable = false)
    private UserPurchase userPurchase;

    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Column(name = "course_name", length = 255, nullable = false)
    private String courseName;
}