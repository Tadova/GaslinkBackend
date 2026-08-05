package com.gaslink.api.modules.product.service;

import com.gaslink.api.exception.BusinessException;
import com.gaslink.api.modules.product.Product;
import com.gaslink.api.modules.product.ProductRepository;
import com.gaslink.api.modules.product.dto.CreateProductRequest;
import com.gaslink.api.modules.product.dto.ProductResponse;
import com.gaslink.api.modules.product.dto.ProductStatisticsDto;
import com.gaslink.api.modules.product.dto.UpdateProductRequest;
import com.gaslink.api.modules.vendor.Vendor;
import com.gaslink.api.modules.vendor.VendorRepository;
import com.gaslink.api.shared.enums.VerificationStatus;
import com.gaslink.api.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final VendorRepository vendorRepository;

    private static final int MAX_PRODUCTS_PER_VENDOR = 10;
    private static final long MAX_IMAGE_SIZE_BYTES = 1_000_000; // 1MB
    private static final long MIN_IMAGE_SIZE_BYTES = 1_000; // 1KB (minimum)

    /**
     * Validate and decode base64 image
     */
    private String validateAndProcessImage(String imageBase64) throws BusinessException {
        // Check if image is provided
        if (imageBase64 == null || imageBase64.isEmpty()) {
            throw new BusinessException("Product image is required. Please upload an image.");
        }

        try {
            // Remove data URL prefix if present
            String base64Data = imageBase64;
            if (imageBase64.contains(",")) {
                base64Data = imageBase64.substring(imageBase64.indexOf(",") + 1);
            }

            // Check if there's actual data after removing prefix
            if (base64Data.trim().isEmpty()) {
                throw new BusinessException("Invalid image format: No image data found");
            }

            // Decode base64 to bytes
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);

            // Check minimum size (1KB - to ensure it's a real image)
            if (imageBytes.length < MIN_IMAGE_SIZE_BYTES) {
                throw new BusinessException("Image seems too small (minimum 1KB). Please upload a valid image.");
            }

            // Check maximum size (1MB)
            if (imageBytes.length > MAX_IMAGE_SIZE_BYTES) {
                throw new BusinessException("Image size must be less than 1MB. Current size: " +
                        (imageBytes.length / 1024) + "KB");
            }

            // Validate that it's an image by checking the first few bytes (magic bytes)
            if (!isValidImageFormat(imageBytes)) {
                throw new BusinessException("Invalid image format. Please upload a valid JPEG, PNG, or GIF image.");
            }

            // Return original base64 string
            return imageBase64;

        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid base64 image format. Please ensure the image is properly encoded.");
        }
    }

    /**
     * Validate image format by checking magic bytes
     */
    private boolean isValidImageFormat(byte[] imageBytes) {
        if (imageBytes.length < 4) {
            return false;
        }

        // Check for JPEG (FF D8 FF)
        if (imageBytes[0] == (byte) 0xFF && imageBytes[1] == (byte) 0xD8 && imageBytes[2] == (byte) 0xFF) {
            return true;
        }

        // Check for PNG (89 50 4E 47)
        if (imageBytes[0] == (byte) 0x89 && imageBytes[1] == (byte) 0x50 &&
                imageBytes[2] == (byte) 0x4E && imageBytes[3] == (byte) 0x47) {
            return true;
        }

        // Check for GIF (47 49 46 38)
        if (imageBytes[0] == (byte) 0x47 && imageBytes[1] == (byte) 0x49 &&
                imageBytes[2] == (byte) 0x46 && imageBytes[3] == (byte) 0x38) {
            return true;
        }

        // Check for WebP (52 49 46 46)
        if (imageBytes[0] == (byte) 0x52 && imageBytes[1] == (byte) 0x49 &&
                imageBytes[2] == (byte) 0x46 && imageBytes[3] == (byte) 0x46) {
            return true;
        }

        return false;
    }

    /**
     * Create a new product
     */
    @Transactional
    public ProductResponse createProduct(UUID vendorId, CreateProductRequest request) throws BusinessException {
        // Validate vendor exists and is verified
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

        if (vendor.getVerificationStatus() != VerificationStatus.VERIFIED) {
            throw new BusinessException("Vendor must be verified to add products");
        }

        // Check product limit (max 10)
        long currentProductCount = productRepository.countByVendorId(vendorId);
        if (currentProductCount >= MAX_PRODUCTS_PER_VENDOR) {
            throw new BusinessException("Maximum product limit reached. You can only have " +
                    MAX_PRODUCTS_PER_VENDOR + " products.");
        }

        // Validate product rules
        validateProductRequest(request);

        // Validate and process image (image is now required)
        String processedImage = validateAndProcessImage(request.getImageBase64());

        // Build product
        Product product = Product.builder()
                .vendorId(vendorId)
                .name(request.getName())
                .description(request.getDescription())
                .isGas(request.isGas())
                .pricePerKg(request.isGas() ? request.getPricePerKg() : null)
                .price(!request.isGas() ? request.getPrice() : null)
                .category(request.getCategory())
                .imageBase64(processedImage)
                .stockQuantity(request.getStockQuantity() != null ? request.getStockQuantity() : 0)
                .weightKg(request.getWeightKg())
                .unit(request.getUnit())
                .isActive(true)
                .isFeatured(request.isFeatured())
                .totalOrders(0)
                .averageRating(0.0)
                .build();

        Product savedProduct = productRepository.save(product);
        log.info("✅ Product created: {} (Type: {}) by vendor: {}",
                savedProduct.getName(),
                savedProduct.isGas() ? "GAS" : "REGULAR",
                vendorId);

        return toResponse(savedProduct);
    }

    /**
     * Validate product request based on type
     */
    private void validateProductRequest(CreateProductRequest request) throws BusinessException {
        if (request.isGas()) {
            // Gas product validation
            if (request.getPricePerKg() == null) {
                throw new BusinessException("Gas products must have a price per kg");
            }
            if (request.getWeightKg() == null) {
                throw new BusinessException("Gas products must specify weight in kg");
            }
            if (request.getUnit() == null) {
                request.setUnit("kg");
            }
        } else {
            // Regular product validation
            if (request.getPrice() == null) {
                throw new BusinessException("Regular products must have a price");
            }
            if (request.getUnit() == null) {
                request.setUnit("piece");
            }
        }
    }

    /**
     * Update a product
     */
    @Transactional
    public ProductResponse updateProduct(UUID vendorId, UUID productId, UpdateProductRequest request) throws BusinessException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        // Verify product belongs to vendor
        if (!product.getVendorId().equals(vendorId)) {
            throw new BusinessException("You don't have permission to update this product");
        }

        // Update fields
        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getIsGas() != null) {
            product.setGas(request.getIsGas());
        }
        if (request.getPricePerKg() != null) {
            product.setPricePerKg(request.getPricePerKg());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getCategory() != null) {
            product.setCategory(request.getCategory());
        }
        if (request.getImageBase64() != null && !request.getImageBase64().isEmpty()) {
            // Image is optional on update, but if provided, validate it
            String processedImage = validateAndProcessImage(request.getImageBase64());
            product.setImageBase64(processedImage);
        }
        if (request.getStockQuantity() != null) {
            product.setStockQuantity(request.getStockQuantity());
        }
        if (request.getIsActive() != null) {
            product.setActive(request.getIsActive());
        }
        if (request.getWeightKg() != null) {
            product.setWeightKg(request.getWeightKg());
        }
        if (request.getUnit() != null) {
            product.setUnit(request.getUnit());
        }
        if (request.getIsFeatured() != null) {
            product.setFeatured(request.getIsFeatured());
        }
        if (request.getAddStockQuantity() != null && request.getAddStockQuantity() > 0) {
            int currentStock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
            product.setStockQuantity(currentStock + request.getAddStockQuantity());
        }

        // Re-validate based on type
        if (product.isGas() && product.getPricePerKg() == null) {
            throw new BusinessException("Gas products must have a price per kg");
        }
        if (!product.isGas() && product.getPrice() == null) {
            throw new BusinessException("Regular products must have a price");
        }

        Product updatedProduct = productRepository.save(product);
        log.info("✅ Product updated: {} by vendor: {}", updatedProduct.getName(), vendorId);

        return toResponse(updatedProduct);
    }

    /**
     * Delete a product (soft delete)
     */
    @Transactional
    public void deleteProduct(UUID vendorId, UUID productId) throws BusinessException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        // Verify product belongs to vendor
        if (!product.getVendorId().equals(vendorId)) {
            throw new BusinessException("You don't have permission to delete this product");
        }

        // Soft delete - deactivate instead of deleting
        product.setActive(false);
        productRepository.save(product);
        log.info("🗑️ Product deleted (soft): {} by vendor: {}", product.getName(), vendorId);
    }

    /**
     * Hard delete a product (permanent)
     */
    @Transactional
    public void hardDeleteProduct(UUID vendorId, UUID productId) throws BusinessException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        // Verify product belongs to vendor
        if (!product.getVendorId().equals(vendorId)) {
            throw new BusinessException("You don't have permission to delete this product");
        }

        productRepository.delete(product);
        log.info("🗑️ Product permanently deleted: {} by vendor: {}", product.getName(), vendorId);
    }

    /**
     * Get all products for a vendor
     */
    public Page<ProductResponse> getVendorProducts(UUID vendorId, Pageable pageable) {
        return productRepository.findByVendorId(vendorId, pageable)
                .map(this::toResponse);
    }

    /**
     * Get all active products for a vendor (public view)
     */
    public List<ProductResponse> getActiveVendorProducts(UUID vendorId) {
        return productRepository.findByVendorIdAndIsActiveTrue(vendorId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get gas products for a vendor
     */
    public List<ProductResponse> getVendorGasProducts(UUID vendorId) {
        return productRepository.findByVendorIdAndIsGasTrue(vendorId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get a single product
     */
    public ProductResponse getProduct(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        return toResponse(product);
    }

    /**
     * Get product statistics for vendor
     */
    public ProductStatisticsDto getProductStatistics(UUID vendorId) {
        Long totalProducts = productRepository.countTotalProducts(vendorId);
        Long activeProducts = productRepository.countActiveProducts(vendorId);

        // Get most popular product
        Pageable topOne = Pageable.ofSize(1);
        List<Product> popularProducts = productRepository.findMostPopularProducts(vendorId, topOne);
        String mostPopular = popularProducts.isEmpty() ? "N/A" : popularProducts.get(0).getName();

        // Calculate total revenue (approximate)
        BigDecimal totalRevenue = BigDecimal.ZERO;
        Long totalOrders = productRepository.sumTotalOrders(vendorId);
        Double avgPrice = productRepository.averageProductPrice(vendorId);

        if (totalOrders != null && avgPrice != null) {
            totalRevenue = BigDecimal.valueOf(totalOrders).multiply(BigDecimal.valueOf(avgPrice));
        }

        // Count low stock and out of stock
        long lowStockCount = productRepository.findLowStockProducts(vendorId, 10).size();
        long outOfStockCount = productRepository.findOutOfStockProducts(vendorId).size();

        return ProductStatisticsDto.builder()
                .totalProducts(totalProducts != null ? totalProducts : 0L)
                .activeProducts(activeProducts != null ? activeProducts : 0L)
                .inactiveProducts((totalProducts != null ? totalProducts : 0L) - (activeProducts != null ? activeProducts : 0L))
                .totalOrders(totalOrders != null ? totalOrders : 0L)
                .averagePrice(avgPrice != null ? BigDecimal.valueOf(avgPrice) : BigDecimal.ZERO)
                .mostPopularProduct(mostPopular)
                .totalRevenue(totalRevenue)
                .lowStockCount(lowStockCount)
                .outOfStockCount(outOfStockCount)
                .usedSlots(totalProducts != null ? totalProducts.intValue() : 0)
                .remainingSlots(MAX_PRODUCTS_PER_VENDOR - (totalProducts != null ? totalProducts.intValue() : 0))
                .maxProductLimit(MAX_PRODUCTS_PER_VENDOR)
                .build();
    }

    /**
     * Get low stock products
     */
    public List<ProductResponse> getLowStockProducts(UUID vendorId, int threshold) {
        return productRepository.findLowStockProducts(vendorId, threshold)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Toggle product active status
     */
    @Transactional
    public ProductResponse toggleProductStatus(UUID vendorId, UUID productId) throws BusinessException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (!product.getVendorId().equals(vendorId)) {
            throw new BusinessException("You don't have permission to update this product");
        }

        product.setActive(!product.isActive());
        Product updated = productRepository.save(product);
        log.info("🔄 Product status toggled: {} - Active: {}", product.getName(), product.isActive());

        return toResponse(updated);
    }

    /**
     * Check if vendor can add more products
     */
    public boolean canAddMoreProducts(UUID vendorId) {
        long currentCount = productRepository.countByVendorId(vendorId);
        return currentCount < MAX_PRODUCTS_PER_VENDOR;
    }

    /**
     * Get remaining product slots
     */
    public int getRemainingProductSlots(UUID vendorId) {
        long currentCount = productRepository.countByVendorId(vendorId);
        return (int) Math.max(0, MAX_PRODUCTS_PER_VENDOR - currentCount);
    }

    /**
     * Convert entity to response DTO
     */
    private ProductResponse toResponse(Product product) {
        // Format display price
        String displayPrice;
        if (product.isGas()) {
            // Gas: show price per kg
            displayPrice = "₦" + product.getPricePerKg().setScale(2, RoundingMode.HALF_UP).toString() + "/kg";
        } else {
            // Regular: show price per unit
            String unit = product.getUnit() != null ? product.getUnit() : "piece";
            displayPrice = "₦" + product.getPrice().setScale(2, RoundingMode.HALF_UP).toString() + "/" + unit;
        }

        return ProductResponse.builder()
                .id(product.getId())
                .vendorId(product.getVendorId())
                .name(product.getName())
                .description(product.getDescription())
                .isGas(product.isGas())
                .price(product.getPrice())
                .pricePerKg(product.getPricePerKg())
                .category(product.getCategory())
                .imageBase64(product.getImageBase64())
                .isActive(product.isActive())
                .stockQuantity(product.getStockQuantity())
                .weightKg(product.getWeightKg())
                .unit(product.getUnit())
                .isFeatured(product.isFeatured())
                .totalOrders(product.getTotalOrders())
                .averageRating(product.getAverageRating())
                .displayPrice(displayPrice)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}