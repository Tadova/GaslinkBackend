package com.gaslink.api.modules.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStats {
    private Long totalOrders;
    private Long pendingOrders;
    private Long processingOrders;
    private Long completedOrders;
    private Long cancelledOrders;
    private Double averageOrderValue;
}