package com.gaslink.api.modules.order;

import com.gaslink.api.shared.audit.AuditableEntity;
import com.gaslink.api.shared.enums.OrderStatus;
import com.gaslink.api.shared.enums.PaymentMethod;
import com.gaslink.api.shared.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "vendor_id")
    private UUID vendorId; // Null until a bid is approved

    @Column(name = "order_reference", unique = true, nullable = false)
    private String orderReference;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    // ===== Gas Order Details =====
    @Column(name = "is_gas_order")
    private boolean isGasOrder = false;

    @Column(name = "gas_quantity_kg")
    private Double gasQuantityKg;

    // ===== Regular Order Details =====
    @Column(name = "total_amount", precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "delivery_fee", precision = 12, scale = 2)
    private BigDecimal deliveryFee = BigDecimal.ZERO;

    @Column(name = "discount", precision = 12, scale = 2)
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(name = "final_amount", precision = 12, scale = 2)
    private BigDecimal finalAmount;

    // ===== Location Details =====
    @Column(name = "customer_lat", nullable = false)
    private Double customerLat;

    @Column(name = "customer_lng", nullable = false)
    private Double customerLng;

    @Column(name = "vendor_lat")
    private Double vendorLat;

    @Column(name = "vendor_lng")
    private Double vendorLng;

    @Column(name = "distance_km")
    private Double distanceKm;

    @Column(name = "delivery_address")
    private String deliveryAddress;

    @Column(name = "delivery_notes")
    private String deliveryNotes;

    @Column(name = "estimated_delivery_time")
    private Integer estimatedDeliveryTime; // in minutes

    // ===== Bidding Details (for gas orders) =====
    @Column(name = "selected_bid_id")
    private UUID selectedBidId;

    @Column(name = "approved_price_per_kg", precision = 12, scale = 2)
    private BigDecimal approvedPricePerKg;

    @Column(name = "approved_delivery_fee", precision = 12, scale = 2)
    private BigDecimal approvedDeliveryFee;

    @Column(name = "approved_total_amount", precision = 12, scale = 2)
    private BigDecimal approvedTotalAmount;

    @Column(name = "bid_deadline")
    private Instant bidDeadline; // When bidding closes (e.g., 30 minutes)

    // ===== Payment Details =====
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    // ===== Timestamps =====
    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "accepted_at")
    private Instant acceptedAt;

    @Column(name = "rejected_at")
    private Instant rejectedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "cancellation_reason")
    private String cancellationReason;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    // ===== Relationships =====
    // FIX: Remove mappedBy and use @JoinColumn
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    @Builder.Default
    private List<OrderBid> bids = new ArrayList<>();

    // FIX: Remove mappedBy and use @JoinColumn
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    // ===== Helper Methods =====
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void setDeliveryFee(BigDecimal deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public void setFinalAmount(BigDecimal finalAmount) {
        this.finalAmount = finalAmount;
    }

    public void setGasQuantityKg(Double gasQuantityKg) {
        this.gasQuantityKg = gasQuantityKg;
    }

    public void setPricePerKg(BigDecimal pricePerKg) {
        this.approvedPricePerKg = pricePerKg;
    }

    public BigDecimal getTotal() {
        return this.totalAmount != null ? this.totalAmount : this.finalAmount;
    }

    public String getReference() {
        return this.orderReference;
    }
}