package com.gaslink.api.modules.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update a product")
public class UpdateProductRequest {

    @Size(min = 2, max = 100, message = "Product name must be between 2 and 100 characters")
    @Schema(description = "Product name", example = "Premium Cooking Gas")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Schema(description = "Product description", example = "High quality cooking gas for home use")
    private String description;

    @Schema(description = "Whether this is a gas product", example = "true")
    private Boolean isGas;

    @DecimalMin(value = "0.01", message = "Price per kg must be greater than 0")
    @Schema(description = "Price per kg (for gas products)", example = "850.00")
    private BigDecimal pricePerKg;

    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Schema(description = "Price per unit (for regular products)", example = "5000.00")
    private BigDecimal price;

    @Schema(description = "Product category", example = "Cooking Gas")
    private String category;

    @Schema(description = "Product image in base64 format (optional on update)",
            example = "data:image/jpeg;base64,/9j/4AAQSkZJRg...")
    private String imageBase64;

    @Min(value = 0, message = "Stock quantity cannot be negative")
    @Schema(description = "New stock quantity (absolute value)", example = "50")
    private Integer stockQuantity;

    @Schema(description = "Whether product is active", example = "true")
    private Boolean isActive;

    @DecimalMin(value = "0.0", message = "Weight must be greater than 0")
    @Schema(description = "Weight in kg (for gas products)", example = "12.5")
    private Double weightKg;

    @Schema(description = "Unit of measurement", example = "piece")
    private String unit;

    @Schema(description = "Whether product is featured", example = "false")
    private Boolean isFeatured;

    @Min(value = 1, message = "Stock quantity to add must be at least 1")
    @Schema(description = "Stock quantity to ADD (incremental, not absolute)", example = "10")
    private Integer addStockQuantity;
}