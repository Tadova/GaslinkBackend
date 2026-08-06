package com.gaslink.api.modules.subscription;

import com.gaslink.api.shared.audit.AuditableEntity;
import com.gaslink.api.shared.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "subscription_payments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPayment extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "subscription_id", nullable = false)
    private UUID subscriptionId;

    @Column(name = "vendor_id", nullable = false)
    private UUID vendorId;

    @Column(name = "reference", unique = true, nullable = false)
    private String reference;

    @Column(name = "gateway_reference")
    private String gatewayReference;

    @Column(precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status = PaymentStatus.INITIATED;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at")
    private Instant createdAt;
}