package com.gaslink.api.modules.subscription.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payment status response")
public class PaymentStatusResponse {
    private boolean success;
    private String status;
    private String message;
    private SubscriptionDto subscription;
}