package com.gaslink.api.modules.message.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Request to share location")
public class ShareLocationRequest {

    @NotNull(message = "Order ID is required")
    @Schema(description = "Order ID", required = true)
    private UUID orderId;

    @NotNull(message = "Receiver ID is required")
    @Schema(description = "Receiver ID", required = true)
    private UUID receiverId;

    @NotNull(message = "Latitude is required")
    @Schema(description = "Latitude", required = true, example = "6.5244")
    private Double lat;

    @NotNull(message = "Longitude is required")
    @Schema(description = "Longitude", required = true, example = "3.3792")
    private Double lng;

    @Schema(description = "Optional message", example = "I am at your location")
    private String message = "I am here";
}