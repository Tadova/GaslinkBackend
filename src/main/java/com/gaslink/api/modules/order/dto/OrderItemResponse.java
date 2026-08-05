package com.gaslink.api.modules.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Order item response")
public class OrderItemResponse {

    @Schema(description = "Product ID")
    private UUID productId;

    @Schema(description = "Product name")
    private String productName;

    @Schema(description = "Quantity")
    private Integer quantity;

    @Schema(description = "Price per unit")
    private BigDecimal price;

    @Schema(description = "Subtotal")
    private BigDecimal subtotal;

    @Schema(description = "Whether this is a gas item")
    private boolean isGasItem;

    @Schema(description = "Weight in kg (for gas)")
    private Double weightKg;

    @Schema(description = "Product image")
    private String imageUrl;
}