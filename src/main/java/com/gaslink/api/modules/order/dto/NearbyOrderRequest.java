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
@Schema(description = "Request to find nearby orders")
public class NearbyOrderRequest {

    @NotNull(message = "Vendor latitude is required")
    @Schema(description = "Vendor latitude", required = true)
    private Double vendorLat;

    @NotNull(message = "Vendor longitude is required")
    @Schema(description = "Vendor longitude", required = true)
    private Double vendorLng;

    @Schema(description = "Search radius in km", example = "10")
    private Double radiusKm = 10.0;
}