package com.gaslink.api.modules.order;

import com.gaslink.api.shared.audit.AuditableEntity;
import com.gaslink.api.shared.enums.BidStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "order_bids")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderBid extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "vendor_id", nullable = false)
    private UUID vendorId;

    @Column(name = "vendor_name")
    private String vendorName;

    @Column(name = "price_per_kg", nullable = false, precision = 12, scale = 2)
    private BigDecimal pricePerKg;

    @Column(name = "delivery_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal deliveryFee;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "estimated_delivery_time")
    private Integer estimatedDeliveryTime; // in minutes

    @Column(name = "delivery_notes")
    private String deliveryNotes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BidStatus status = BidStatus.PENDING;

    @Column(name = "expires_at")
    private Instant expiresAt; // This is the field used for expiry

    @Column(name = "distance_km")
    private Double distanceKm;

    @Column(name = "vendor_lat")
    private Double vendorLat;

    @Column(name = "vendor_lng")
    private Double vendorLng;
}