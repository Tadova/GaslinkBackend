package com.gaslink.api.modules.subscription.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to initiate subscription payment")
public class InitiateSubscriptionPaymentRequest {

    @NotBlank(message = "Plan is required")
    @Schema(description = "Subscription plan", required = true, allowableValues = {"BASIC", "PREMIUM"}, example = "BASIC")
    private String plan;

    @NotBlank(message = "Callback URL is required")
    @Schema(description = "Payment callback URL", required = true, example = "gaslink://payment-success")
    private String callbackUrl;
}