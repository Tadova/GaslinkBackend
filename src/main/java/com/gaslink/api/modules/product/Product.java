package com.gaslink.api.modules.product;

import com.gaslink.api.shared.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "vendor_id", nullable = false)
    private UUID vendorId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "price", precision = 12, scale = 2)
    private BigDecimal price; // For regular products (per unit)

    @Column(name = "is_gas")
    private boolean isGas = false;

    @Column(name = "price_per_kg", precision = 12, scale = 2)
    private BigDecimal pricePerKg; // For gas products (per kg)

    @Column(name = "category")
    private String category;

    @Column(name = "image_base64", columnDefinition = "TEXT")
    private String imageBase64;

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "stock_quantity")
    private Integer stockQuantity = 0;

    @Column(name = "weight_kg")
    private Double weightKg;

    @Column(name = "unit")
    private String unit; // e.g., "kg", "piece", "litre", "bottle"

    @Column(name = "total_orders")
    private Integer totalOrders = 0;

    @Column(name = "average_rating")
    private Double averageRating = 0.0;

    @Column(name = "is_featured")
    private boolean isFeatured = false;
}