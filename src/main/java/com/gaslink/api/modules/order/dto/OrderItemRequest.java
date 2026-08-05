package com.gaslink.api.modules.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Order item request")
public class OrderItemRequest {

    @NotNull(message = "Product ID is required")
    @Schema(description = "Product ID", required = true)
    private UUID productId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Minimum quantity is 1")
    @Schema(description = "Quantity", required = true, example = "2")
    private Integer quantity;
}