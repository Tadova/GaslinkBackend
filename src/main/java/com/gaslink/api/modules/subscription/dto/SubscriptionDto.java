package com.gaslink.api.modules.subscription.dto;

import com.gaslink.api.shared.enums.SubscriptionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Subscription details response")
public class SubscriptionDto {

    @Schema(description = "Subscription unique identifier", example = "sub_550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Vendor unique identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID vendorId;

    @Schema(
            description = "Subscription plan name",
            allowableValues = {"FREE_TRIAL", "BASIC", "PREMIUM"},
            example = "PREMIUM"
    )
    private String plan;

    @Schema(
            description = "Subscription amount in NGN",
            example = "5000.00"
    )
    private BigDecimal amount;

    @Schema(
            description = "Billing cycle",
            allowableValues = {"MONTHLY", "ANNUAL"},
            example = "MONTHLY"
    )
    private String billingCycle;

    @Schema(
            description = "Current subscription status",
            allowableValues = {"ACTIVE", "EXPIRED", "CANCELLED", "PENDING", "FREE_TRIAL"},
            example = "ACTIVE"
    )
    private SubscriptionStatus status;

    @Schema(
            description = "Subscription start date (ISO 8601)",
            example = "2026-08-05T10:00:00Z"
    )
    private Instant startedAt;

    @Schema(
            description = "Subscription expiry date (ISO 8601)",
            example = "2026-09-05T10:00:00Z"
    )
    private Instant expiresAt;
}