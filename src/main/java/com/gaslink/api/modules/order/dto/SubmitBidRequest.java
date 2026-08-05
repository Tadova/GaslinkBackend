package com.gaslink.api.modules.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to submit a bid for a gas order (vendor)")
public class SubmitBidRequest {

    @NotNull(message = "Price per kg is required")
    @Min(value = 1, message = "Price per kg must be greater than 0")
    @Schema(description = "Price per kg offered by vendor", required = true, example = "850.00")
    private BigDecimal pricePerKg;

    @NotNull(message = "Delivery fee is required")
    @Min(value = 0, message = "Delivery fee cannot be negative")
    @Schema(description = "Delivery fee", required = true, example = "500.00")
    private BigDecimal deliveryFee;

    @Min(value = 1, message = "Estimated delivery time must be at least 1 minute")
    @Schema(description = "Estimated delivery time in minutes", example = "30")
    private Integer estimatedDeliveryTime;

    @Schema(description = "Delivery notes")
    private String deliveryNotes;
}