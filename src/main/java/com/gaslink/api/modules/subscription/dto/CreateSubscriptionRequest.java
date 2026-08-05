package com.gaslink.api.modules.subscription.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for initiating subscription payment")
public class CreateSubscriptionRequest {

    @NotBlank(message = "Plan is required")
    @Schema(
            description = "Subscription plan. BASIC = Monthly (₦5,000/month), PREMIUM = Annual (₦50,000/year)",
            required = true,
            allowableValues = {"BASIC", "PREMIUM"},
            example = "BASIC"
    )
    private String plan;

    @Schema(
            description = "Billing cycle (auto-determined by plan)",
            hidden = true
    )
    private String billingCycle; // Not needed - determined by plan

    @NotBlank(message = "Callback URL is required")
    @Schema(
            description = "Paystack callback URL (deep link for mobile app)",
            required = true,
            example = "gaslink://payment-success"
    )
    private String callbackUrl;
}