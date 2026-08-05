package com.gaslink.api.modules.product.dto;

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
@Schema(description = "Product response")
public class ProductResponse {

    @Schema(description = "Product ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Vendor ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID vendorId;

    @Schema(description = "Product name", example = "Premium Cooking Gas")
    private String name;

    @Schema(description = "Product description", example = "High quality cooking gas for home use")
    private String description;

    @Schema(description = "Whether this is a gas product", example = "true")
    private boolean isGas;

    @Schema(description = "Price per unit (for regular products)", example = "5000.00")
    private BigDecimal price;

    @Schema(description = "Price per kg (for gas products)", example = "850.00")
    private BigDecimal pricePerKg;

    @Schema(description = "Product category", example = "Cooking Gas")
    private String category;

    @Schema(description = "Product image (base64)")
    private String imageBase64;

    @Schema(description = "Whether product is active", example = "true")
    private boolean isActive;

    @Schema(description = "Stock quantity", example = "50")
    private Integer stockQuantity;

    @Schema(description = "Weight in kg (for gas products)", example = "12.5")
    private Double weightKg;

    @Schema(description = "Unit of measurement", example = "kg")
    private String unit;

    @Schema(description = "Whether product is featured", example = "false")
    private boolean isFeatured;

    @Schema(description = "Total orders for this product", example = "150")
    private Integer totalOrders;

    @Schema(description = "Average rating", example = "4.5")
    private Double averageRating;

    @Schema(description = "Display price (formatted)", example = "₦850/kg")
    private String displayPrice;

    @Schema(description = "Created at timestamp")
    private Instant createdAt;

    @Schema(description = "Updated at timestamp")
    private Instant updatedAt;
}