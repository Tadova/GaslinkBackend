package com.gaslink.api.modules.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Order with bids response")
public class OrderBidResponse {

    @Schema(description = "Order ID")
    private UUID id;

    @Schema(description = "Order reference")
    private String orderReference;

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

    @Schema(description = "Gas quantity in kg")
    private Double gasQuantityKg;

    @Schema(description = "Delivery address")
    private String deliveryAddress;

    @Schema(description = "Delivery notes")
    private String deliveryNotes;

    @Schema(description = "Order status")
    private String status;

    @Schema(description = "Bid deadline")
    private Instant bidDeadline;

    @Schema(description = "Selected bid ID (if approved)")
    private UUID selectedBidId;

    @Schema(description = "Approved price per kg")
    private BigDecimal approvedPricePerKg;

    @Schema(description = "Approved delivery fee")
    private BigDecimal approvedDeliveryFee;

    @Schema(description = "Approved total amount")
    private BigDecimal approvedTotalAmount;

    @Schema(description = "All bids for this order")
    private List<BidResponse> bids;

    @Schema(description = "Created at")
    private Instant createdAt;

    @Schema(description = "Approved at")
    private Instant approvedAt;
}