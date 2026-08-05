package com.gaslink.api.modules.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    // ============================================================
    // Basic Queries
    // ============================================================

    /**
     * Find all products by vendor ID
     */
    List<Product> findByVendorId(UUID vendorId);

    /**
     * Find products by vendor ID with pagination
     */
    Page<Product> findByVendorId(UUID vendorId, Pageable pageable);

    /**
     * Find active products by vendor ID
     */
    List<Product> findByVendorIdAndIsActiveTrue(UUID vendorId);

    /**
     * Find active products by vendor ID with pagination
     */
    Page<Product> findByVendorIdAndIsActiveTrue(UUID vendorId, Pageable pageable);

    /**
     * Find gas products by vendor ID
     */
    List<Product> findByVendorIdAndIsGasTrue(UUID vendorId);

    /**
     * Find non-gas products by vendor ID
     */
    List<Product> findByVendorIdAndIsGasFalse(UUID vendorId);

    /**
     * Find products by category
     */
    List<Product> findByCategory(String category);

    /**
     * Find products by vendor and category
     */
    List<Product> findByVendorIdAndCategory(UUID vendorId, String category);

    // ============================================================
    // Count Queries
    // ============================================================

    /**
     * Count total products by vendor
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.vendorId = :vendorId")
    Long countTotalProducts(@Param("vendorId") UUID vendorId);

    /**
     * Count active products by vendor
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.vendorId = :vendorId AND p.isActive = true")
    Long countActiveProducts(@Param("vendorId") UUID vendorId);

    /**
     * Count products by vendor and type
     */
    long countByVendorIdAndIsGas(UUID vendorId, boolean isGas);

    /**
     * Count total products by vendor (simple version)
     */
    long countByVendorId(UUID vendorId);

    // ============================================================
    // Stock Queries
    // ============================================================

    /**
     * Find products with stock above minimum
     */
    List<Product> findByVendorIdAndStockQuantityGreaterThan(UUID vendorId, Integer minStock);

    /**
     * Find low stock products (below threshold)
     */
    @Query("SELECT p FROM Product p WHERE p.vendorId = :vendorId AND p.stockQuantity <= :threshold AND p.isActive = true")
    List<Product> findLowStockProducts(@Param("vendorId") UUID vendorId, @Param("threshold") Integer threshold);

    /**
     * Find out of stock products
     */
    @Query("SELECT p FROM Product p WHERE p.vendorId = :vendorId AND p.stockQuantity <= 0 AND p.isActive = true")
    List<Product> findOutOfStockProducts(@Param("vendorId") UUID vendorId);

    /**
     * Find products with stock (in stock)
     */
    @Query("SELECT p FROM Product p WHERE p.vendorId = :vendorId AND p.stockQuantity > 0 AND p.isActive = true")
    List<Product> findInStockProducts(@Param("vendorId") UUID vendorId);

    // ============================================================
    // Price Queries
    // ============================================================

    /**
     * Find products by price range
     */
    List<Product> findByVendorIdAndPriceBetween(UUID vendorId, BigDecimal minPrice, BigDecimal maxPrice);

    /**
     * Find products by price per kg range (gas products)
     */
    List<Product> findByVendorIdAndPricePerKgBetween(UUID vendorId, BigDecimal minPrice, BigDecimal maxPrice);

    /**
     * Get average price of all products for a vendor
     */
    @Query("SELECT AVG(p.price) FROM Product p WHERE p.vendorId = :vendorId AND p.isGas = false")
    Double averageProductPrice(@Param("vendorId") UUID vendorId);

    /**
     * Get average price per kg of gas products for a vendor
     */
    @Query("SELECT AVG(p.pricePerKg) FROM Product p WHERE p.vendorId = :vendorId AND p.isGas = true")
    Double averageGasPrice(@Param("vendorId") UUID vendorId);

    /**
     * Get minimum product price for a vendor
     */
    @Query("SELECT MIN(p.price) FROM Product p WHERE p.vendorId = :vendorId AND p.isGas = false")
    BigDecimal findMinPrice(@Param("vendorId") UUID vendorId);

    /**
     * Get maximum product price for a vendor
     */
    @Query("SELECT MAX(p.price) FROM Product p WHERE p.vendorId = :vendorId AND p.isGas = false")
    BigDecimal findMaxPrice(@Param("vendorId") UUID vendorId);

    // ============================================================
    // Search Queries
    // ============================================================

    /**
     * Search products by name (case insensitive)
     */
    @Query("SELECT p FROM Product p WHERE p.vendorId = :vendorId AND LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Product> searchProducts(@Param("vendorId") UUID vendorId, @Param("searchTerm") String searchTerm);

    /**
     * Search products by name and category (case insensitive)
     */
    @Query("SELECT p FROM Product p WHERE p.vendorId = :vendorId AND " +
            "(LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.category) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Product> searchProductsByNameOrCategory(@Param("vendorId") UUID vendorId, @Param("searchTerm") String searchTerm);

    /**
     * Search active products (public)
     */
    @Query("SELECT p FROM Product p WHERE p.vendorId = :vendorId AND p.isActive = true AND " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Product> searchActiveProducts(@Param("vendorId") UUID vendorId, @Param("searchTerm") String searchTerm);

    // ============================================================
    // Popularity & Rating Queries
    // ============================================================

    /**
     * Get most popular products by total orders
     */
    @Query("SELECT p FROM Product p WHERE p.vendorId = :vendorId ORDER BY p.totalOrders DESC")
    List<Product> findMostPopularProducts(@Param("vendorId") UUID vendorId, Pageable pageable);

    /**
     * Get highest rated products
     */
    @Query("SELECT p FROM Product p WHERE p.vendorId = :vendorId ORDER BY p.averageRating DESC")
    List<Product> findHighestRatedProducts(@Param("vendorId") UUID vendorId, Pageable pageable);

    /**
     * Get featured products
     */
    List<Product> findByVendorIdAndIsFeaturedTrue(UUID vendorId);

    /**
     * Sum total orders for a vendor
     */
    @Query("SELECT SUM(p.totalOrders) FROM Product p WHERE p.vendorId = :vendorId")
    Long sumTotalOrders(@Param("vendorId") UUID vendorId);

    // ============================================================
    // Gas Product Specific Queries
    // ============================================================

    /**
     * Get gas products with price per kg
     */
    @Query("SELECT p FROM Product p WHERE p.vendorId = :vendorId AND p.isGas = true AND p.isActive = true")
    List<Product> findActiveGasProducts(@Param("vendorId") UUID vendorId);

    /**
     * Get gas products with price per kg sorted by price (cheapest first)
     */
    @Query("SELECT p FROM Product p WHERE p.vendorId = :vendorId AND p.isGas = true AND p.isActive = true ORDER BY p.pricePerKg ASC")
    List<Product> findCheapestGasProducts(@Param("vendorId") UUID vendorId);

    // ============================================================
    // Verification Queries
    // ============================================================

    /**
     * Check if a product belongs to a vendor
     */
    boolean existsByIdAndVendorId(UUID id, UUID vendorId);

    /**
     * Check if a product exists and is active
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Product p WHERE p.id = :id AND p.isActive = true")
    boolean isProductActive(@Param("id") UUID id);

    /**
     * Check if a product is in stock
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Product p WHERE p.id = :id AND p.stockQuantity > 0")
    boolean isProductInStock(@Param("id") UUID id);

    /**
     * Check if a vendor has any active products
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Product p WHERE p.vendorId = :vendorId AND p.isActive = true")
    boolean vendorHasActiveProducts(@Param("vendorId") UUID vendorId);

    /**
     * Check if a vendor has any gas products
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Product p WHERE p.vendorId = :vendorId AND p.isGas = true")
    boolean vendorHasGasProducts(@Param("vendorId") UUID vendorId);

    // ============================================================
    // Batch Operations
    // ============================================================

    /**
     * Update product stock after order
     */
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity - :quantity WHERE p.id = :productId")
    void deductStock(@Param("productId") UUID productId, @Param("quantity") Integer quantity);

    /**
     * Update product total orders
     */
    @Query("UPDATE Product p SET p.totalOrders = p.totalOrders + 1 WHERE p.id = :productId")
    void incrementTotalOrders(@Param("productId") UUID productId);

    /**
     * Update product rating
     */
    @Query("UPDATE Product p SET p.averageRating = :rating WHERE p.id = :productId")
    void updateRating(@Param("productId") UUID productId, @Param("rating") Double rating);

    // ============================================================
    // Analytics Queries
    // ============================================================

    /**
     * Get product count by category
     */
    @Query("SELECT p.category, COUNT(p) FROM Product p WHERE p.vendorId = :vendorId GROUP BY p.category")
    List<Object[]> countProductsByCategory(@Param("vendorId") UUID vendorId);

    /**
     * Get total revenue by product
     */
    @Query("SELECT p.name, p.totalOrders * p.price FROM Product p WHERE p.vendorId = :vendorId AND p.isGas = false")
    List<Object[]> getProductRevenue(@Param("vendorId") UUID vendorId);

    /**
     * Get total gas revenue
     */
    @Query("SELECT SUM(p.totalOrders * p.pricePerKg) FROM Product p WHERE p.vendorId = :vendorId AND p.isGas = true")
    Double getTotalGasRevenue(@Param("vendorId") UUID vendorId);

    /**
     * Get total product revenue (non-gas)
     */
    @Query("SELECT SUM(p.totalOrders * p.price) FROM Product p WHERE p.vendorId = :vendorId AND p.isGas = false")
    Double getTotalProductRevenue(@Param("vendorId") UUID vendorId);

    // ============================================================
    // Featured Products
    // ============================================================

    /**
     * Get featured products by vendor
     */
    @Query("SELECT p FROM Product p WHERE p.vendorId = :vendorId AND p.isFeatured = true AND p.isActive = true ORDER BY p.totalOrders DESC")
    List<Product> findFeaturedProducts(@Param("vendorId") UUID vendorId);

    /**
     * Get featured gas products
     */
    @Query("SELECT p FROM Product p WHERE p.vendorId = :vendorId AND p.isFeatured = true AND p.isGas = true AND p.isActive = true")
    List<Product> findFeaturedGasProducts(@Param("vendorId") UUID vendorId);

    // ============================================================
    // Public Queries (No Auth)
    // ============================================================

    /**
     * Get all active products for a vendor (public)
     */
    @Query("SELECT p FROM Product p WHERE p.vendorId = :vendorId AND p.isActive = true")
    List<Product> findPublicVendorProducts(@Param("vendorId") UUID vendorId);

    /**
     * Get all active gas products for a vendor (public)
     */
    @Query("SELECT p FROM Product p WHERE p.vendorId = :vendorId AND p.isActive = true AND p.isGas = true")
    List<Product> findPublicGasProducts(@Param("vendorId") UUID vendorId);

    /**
     * Get all active non-gas products for a vendor (public)
     */
    @Query("SELECT p FROM Product p WHERE p.vendorId = :vendorId AND p.isActive = true AND p.isGas = false")
    List<Product> findPublicNonGasProducts(@Param("vendorId") UUID vendorId);
}