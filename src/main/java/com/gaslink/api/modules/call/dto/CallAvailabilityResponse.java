package com.gaslink.api.modules.call.dto;

import com.gaslink.api.shared.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallAvailabilityResponse {
    private boolean available;
    private String message;
    private UUID orderId;
    private String orderReference;
    private OrderStatus orderStatus;
    private UUID vendorId;
    private String vendorName;
    private UUID customerId;
    private String customerName;
}