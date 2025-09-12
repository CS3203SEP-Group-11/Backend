package com.levelup.paymentservice.model;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "invoices")
public class Invoice {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "invoice_number", nullable = false, unique = true, length = 50)
    private String invoiceNumber;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "subscription_id")
    private UUID subscriptionId;

    @Column(name = "purchase_id")
    private UUID purchaseId;

    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "tax_amount", precision = 10, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "USD";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InvoiceStatus status;

    @Column(name = "stripe_invoice_id", length = 255)
    private String stripeInvoiceId;

    @Column(name = "billing_period_start")
    private LocalDateTime billingPeriodStart;

    @Column(name = "billing_period_end")
    private LocalDateTime billingPeriodEnd;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "invoice_url", length = 500)
    private String invoiceUrl;

    @Column(name = "pdf_url", length = 500)
    private String pdfUrl;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public Invoice() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = InvoiceStatus.DRAFT;
        this.currency = "USD";
        this.taxAmount = BigDecimal.ZERO;
        this.discountAmount = BigDecimal.ZERO;
    }

    public Invoice(String invoiceNumber, UUID userId, BigDecimal subtotal) {
        this();
        this.invoiceNumber = invoiceNumber;
        this.userId = userId;
        this.subtotal = subtotal;
        this.totalAmount = subtotal;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(UUID subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public UUID getPurchaseId() {
        return purchaseId;
    }

    public void setPurchaseId(UUID purchaseId) {
        this.purchaseId = purchaseId;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
        calculateTotal();
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
        calculateTotal();
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
        calculateTotal();
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public void setStatus(InvoiceStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
        if (status == InvoiceStatus.PAID && paidAt == null) {
            this.paidAt = LocalDateTime.now();
        }
    }

    public String getStripeInvoiceId() {
        return stripeInvoiceId;
    }

    public void setStripeInvoiceId(String stripeInvoiceId) {
        this.stripeInvoiceId = stripeInvoiceId;
    }

    public LocalDateTime getBillingPeriodStart() {
        return billingPeriodStart;
    }

    public void setBillingPeriodStart(LocalDateTime billingPeriodStart) {
        this.billingPeriodStart = billingPeriodStart;
    }

    public LocalDateTime getBillingPeriodEnd() {
        return billingPeriodEnd;
    }

    public void setBillingPeriodEnd(LocalDateTime billingPeriodEnd) {
        this.billingPeriodEnd = billingPeriodEnd;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public String getInvoiceUrl() {
        return invoiceUrl;
    }

    public void setInvoiceUrl(String invoiceUrl) {
        this.invoiceUrl = invoiceUrl;
    }

    public String getPdfUrl() {
        return pdfUrl;
    }

    public void setPdfUrl(String pdfUrl) {
        this.pdfUrl = pdfUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Helper methods
    private void calculateTotal() {
        if (subtotal != null && taxAmount != null && discountAmount != null) {
            this.totalAmount = subtotal.add(taxAmount).subtract(discountAmount);
        }
    }

    public boolean isOverdue() {
        return status != InvoiceStatus.PAID && dueDate != null && LocalDateTime.now().isAfter(dueDate);
    }

    public boolean isRecurring() {
        return subscriptionId != null;
    }

    public boolean isOneTime() {
        return purchaseId != null;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Enum
    public enum InvoiceStatus {
        DRAFT, OPEN, PAID, VOID, UNCOLLECTIBLE
    }
}
