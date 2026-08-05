package com.gaslink.api.modules.product.dto;

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
@Schema(description = "Product statistics for vendor dashboard")
public class ProductStatisticsDto {

    @Schema(description = "Total products", example = "10")
    private Long totalProducts;

    @Schema(description = "Active products", example = "8")
    private Long activeProducts;

    @Schema(description = "Inactive products", example = "2")
    private Long inactiveProducts;

    @Schema(description = "Total gas products", example = "1")
    private Long gasProducts;

    @Schema(description = "Total regular products", example = "9")
    private Long regularProducts;

    @Schema(description = "Total orders across all products", example = "500")
    private Long totalOrders;

    @Schema(description = "Average product price", example = "4500.00")
    private BigDecimal averagePrice;

    @Schema(description = "Average gas price per kg", example = "850.00")
    private BigDecimal averageGasPrice;

    @Schema(description = "Most popular product name", example = "Premium Cooking Gas")
    private String mostPopularProduct;

    @Schema(description = "Total revenue from all products", example = "2500000.00")
    private BigDecimal totalRevenue;

    @Schema(description = "Low stock products count", example = "3")
    private Long lowStockCount;

    @Schema(description = "Out of stock products count", example = "1")
    private Long outOfStockCount;

    @Schema(description = "Used product slots", example = "8")
    private Integer usedSlots;

    @Schema(description = "Remaining product slots", example = "2")
    private Integer remainingSlots;

    @Schema(description = "Max product limit", example = "10")
    private Integer maxProductLimit = 10;
}