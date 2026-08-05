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
@Schema(description = "Gas order request")
public class GasOrderRequest {

    @NotNull(message = "Gas quantity is required")
    @Min(value = 1, message = "Minimum gas quantity is 1kg")
    @Schema(description = "Gas quantity in kg", required = true, example = "12.5")
    private Double quantityKg;
}