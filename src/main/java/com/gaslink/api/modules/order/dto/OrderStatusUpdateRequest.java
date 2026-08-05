package com.gaslink.api.modules.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update order status")
public class OrderStatusUpdateRequest {

    @NotNull(message = "Status is required")
    @Schema(description = "New order status", required = true,
            allowableValues = {"ACCEPTED", "REJECTED", "PROCESSING", "READY", "COMPLETED"})
    private String status;

    @Schema(description = "Reason for rejection (required if status is REJECTED)")
    private String reason;
}