package com.gaslink.api.modules.order.dto;

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
@Schema(description = "Nearby order response for vendors")
public class NearbyOrderResponse {

    @Schema(description = "Order ID")
    private UUID orderId;

    @Schema(description = "Order reference number")
    private String orderReference; // ADD THIS FIELD

    @Schema(description = "Customer ID")
    private UUID customerId;

    @Schema(description = "Customer name")
    private String customerName;

    @Schema(description = "Customer phone")
    private String customerPhone;

    @Schema(description = "Customer latitude")
    private Double customerLat;

    @Schema(description = "Customer longitude")
    private Double customerLng;

    @Schema(description = "Total order amount")
    private BigDecimal totalAmount;

    @Schema(description = "Whether this is a gas order")
    private boolean isGasOrder;

    @Schema(description = "Gas quantity in kg")
    private Double gasQuantityKg;

    @Schema(description = "Delivery address")
    private String deliveryAddress;

    @Schema(description = "Delivery notes")
    private String deliveryNotes;

    @Schema(description = "Distance from vendor in km")
    private Double distanceKm;

    @Schema(description = "Bid deadline")
    private Instant bidDeadline;

    @Schema(description = "Order placed at")
    private Instant createdAt;

    @Schema(description = "Total bids received")
    private Long totalBids;
}