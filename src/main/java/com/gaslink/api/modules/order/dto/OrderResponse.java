package com.gaslink.api.modules.order.dto;

import com.gaslink.api.shared.enums.OrderStatus;
import com.gaslink.api.shared.enums.PaymentMethod;
import com.gaslink.api.shared.enums.PaymentStatus;
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
@Schema(description = "Order response")
public class OrderResponse {

    @Schema(description = "Order ID")
    private UUID id;

    @Schema(description = "Order reference number")
    private String orderReference;

    @Schema(description = "Customer ID")
    private UUID customerId;

    @Schema(description = "Customer name")
    private String customerName;

    @Schema(description = "Customer latitude")
    private Double customerLat;

    @Schema(description = "Customer longitude")
    private Double customerLng;

    @Schema(description = "Vendor ID")
    private UUID vendorId;

    @Schema(description = "Vendor name")
    private String vendorName;

    @Schema(description = "Order status")
    private OrderStatus status;

    @Schema(description = "Total amount")
    private BigDecimal totalAmount;

    @Schema(description = "Delivery fee")
    private BigDecimal deliveryFee;

    @Schema(description = "Discount")
    private BigDecimal discount;

    @Schema(description = "Final amount")
    private BigDecimal finalAmount;

    @Schema(description = "Payment method")
    private PaymentMethod paymentMethod;

    @Schema(description = "Payment status")
    private PaymentStatus paymentStatus;

    @Schema(description = "Delivery address")
    private String deliveryAddress;

    @Schema(description = "Delivery notes")
    private String deliveryNotes;

    @Schema(description = "Distance from customer to vendor in km")
    private Double distanceKm;

    @Schema(description = "Estimated delivery time in minutes")
    private Integer estimatedDeliveryTime;

    @Schema(description = "Whether this is a gas order")
    private boolean isGasOrder;

    @Schema(description = "Gas quantity in kg")
    private Double gasQuantityKg;

    @Schema(description = "Price per kg")
    private BigDecimal pricePerKg;

    @Schema(description = "Order items")
    private List<OrderItemResponse> items;

    @Schema(description = "Bid deadline")
    private Instant bidDeadline;

    @Schema(description = "Selected bid ID")
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

    @Schema(description = "Accepted at")
    private Instant acceptedAt;

    @Schema(description = "Rejected at")
    private Instant rejectedAt;

    @Schema(description = "Rejection reason")
    private String rejectionReason;

    @Schema(description = "Completed at")
    private Instant completedAt;

    @Schema(description = "Cancelled at")
    private Instant cancelledAt;

    @Schema(description = "Cancellation reason")
    private String cancellationReason;
}