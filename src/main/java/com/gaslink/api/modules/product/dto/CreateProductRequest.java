package com.gaslink.api.modules.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new product")
public class CreateProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 100, message = "Product name must be between 2 and 100 characters")
    @Schema(description = "Product name", required = true, example = "Premium Cooking Gas")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Schema(description = "Product description", example = "High quality cooking gas for home use")
    private String description;

    @NotNull(message = "Product type (isGas) is required")
    @Schema(description = "Whether this is a gas product", required = true, example = "true")
    private boolean isGas = false;

    // For GAS products
    @DecimalMin(value = "0.01", message = "Price per kg must be greater than 0")
    @Schema(description = "Price per kg (REQUIRED for gas products)", example = "850.00")
    private BigDecimal pricePerKg;

    // For REGULAR products
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Schema(description = "Price per unit (REQUIRED for regular products)", example = "5000.00")
    private BigDecimal price;

    @Schema(description = "Product category", example = "Cooking Gas")
    private String category;

    @NotBlank(message = "Product image is required")
    @Schema(description = "Product image in base64 format (max 1MB)",
            required = true,
            example = "data:image/jpeg;base64,/9j/4AAQSkZJRg...")
    private String imageBase64;

    @Min(value = 0, message = "Stock quantity cannot be negative")
    @Schema(description = "Stock quantity", example = "50")
    private Integer stockQuantity = 0;

    @DecimalMin(value = "0.0", message = "Weight must be greater than 0")
    @Schema(description = "Weight in kg (for gas products)", example = "12.5")
    private Double weightKg;

    @Schema(description = "Unit of measurement", example = "kg")
    private String unit;

    @Schema(description = "Whether product is featured", example = "false")
    private boolean isFeatured = false;
}