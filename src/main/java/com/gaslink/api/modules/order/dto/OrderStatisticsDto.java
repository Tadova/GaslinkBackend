package com.gaslink.api.modules.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Order statistics for vendor dashboard")
public class OrderStatisticsDto {

    @Schema(description = "Total orders")
    private Long totalOrders;

    @Schema(description = "Pending orders")
    private Long pendingOrders;

    @Schema(description = "Accepted orders")
    private Long acceptedOrders;

    @Schema(description = "Processing orders")
    private Long processingOrders;

    @Schema(description = "Ready orders")
    private Long readyOrders;

    @Schema(description = "Completed orders")
    private Long completedOrders;

    @Schema(description = "Rejected orders")
    private Long rejectedOrders;

    @Schema(description = "Cancelled orders")
    private Long cancelledOrders;

    @Schema(description = "Total revenue")
    private BigDecimal totalRevenue;
}