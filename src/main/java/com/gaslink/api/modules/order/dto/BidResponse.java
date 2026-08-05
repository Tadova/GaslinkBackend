package com.gaslink.api.modules.order.dto;

import com.gaslink.api.shared.enums.BidStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Bid response")
public class BidResponse {

    @Schema(description = "Bid ID")
    private UUID id;

    @Schema(description = "Order ID")
    private UUID orderId;

    @Schema(description = "Vendor ID")
    private UUID vendorId;

    @Schema(description = "Vendor name")
    private String vendorName;

    @Schema(description = "Vendor business name")
    private String businessName;

    @Schema(description = "Vendor rating")
    private Double vendorRating;

    @Schema(description = "Price per kg offered")
    private BigDecimal pricePerKg;

    @Schema(description = "Delivery fee")
    private BigDecimal deliveryFee;

    @Schema(description = "Total amount")
    private BigDecimal totalAmount;

    @Schema(description = "Estimated delivery time in minutes")
    private Integer estimatedDeliveryTime;

    @Schema(description = "Delivery notes")
    private String deliveryNotes;

    @Schema(description = "Distance from vendor to customer in km")
    private Double distanceKm;

    @Schema(description = "Bid status")
    private BidStatus status;

    @Schema(description = "Bid expires at")
    private Instant expiresAt;

    @Schema(description = "Created at")
    private Instant createdAt;
}