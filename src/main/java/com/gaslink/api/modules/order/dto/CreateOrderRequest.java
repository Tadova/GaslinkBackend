package com.gaslink.api.modules.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new order")
public class CreateOrderRequest {

    @NotNull(message = "Vendor ID is required")
    @Schema(description = "Vendor ID", required = true)
    private UUID vendorId;

    @Schema(description = "Delivery address")
    private String deliveryAddress;

    @NotNull(message = "Customer latitude is required")
    @Schema(description = "Customer latitude", required = true, example = "6.5244")
    private Double customerLat;

    @NotNull(message = "Customer longitude is required")
    @Schema(description = "Customer longitude", required = true, example = "3.3792")
    private Double customerLng;

    @Schema(description = "Delivery notes")
    private String deliveryNotes;

    @Schema(description = "Gas order details (for gas orders)")
    private GasOrderRequest gasOrder;

    @Schema(description = "Product order items (for regular products)")
    private List<OrderItemRequest> items;

    @Schema(description = "Payment method", example = "CARD")
    private String paymentMethod;

    @Schema(description = "Payment callback URL")
    private String callbackUrl;
}