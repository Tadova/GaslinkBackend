package com.gaslink.api.modules.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a gas order (customer)")
public class CreateGasOrderRequest {

    @NotNull(message = "Gas quantity is required")
    @Min(value = 1, message = "Minimum gas quantity is 1kg")
    @Schema(description = "Gas quantity in kg", required = true, example = "12.5")
    private Double quantityKg;

    @NotNull(message = "Customer latitude is required")
    @Schema(description = "Customer latitude", required = true, example = "6.5244")
    private Double customerLat;

    @NotNull(message = "Customer longitude is required")
    @Schema(description = "Customer longitude", required = true, example = "3.3792")
    private Double customerLng;

    @Schema(description = "Delivery address")
    private String deliveryAddress;

    @Schema(description = "Delivery notes")
    private String deliveryNotes;

    @Schema(description = "Bid deadline in minutes (default: 30)", example = "30")
    private Integer bidDeadlineMinutes = 30;
}