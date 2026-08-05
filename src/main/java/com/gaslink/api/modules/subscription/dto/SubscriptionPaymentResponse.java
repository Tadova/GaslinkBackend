package com.gaslink.api.modules.subscription.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPaymentResponse {
    private String authorizationUrl;
    private String reference;
    private String accessCode;
}