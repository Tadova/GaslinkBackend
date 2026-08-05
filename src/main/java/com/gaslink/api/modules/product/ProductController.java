package com.gaslink.api.modules.product;

import com.gaslink.api.exception.BusinessException;
import com.gaslink.api.modules.product.dto.CreateProductRequest;
import com.gaslink.api.modules.product.dto.ProductResponse;
import com.gaslink.api.modules.product.dto.ProductStatisticsDto;
import com.gaslink.api.modules.product.dto.UpdateProductRequest;
import com.gaslink.api.modules.product.service.ProductService;
import com.gaslink.api.response.ApiResponse; // Your custom response class
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
// REMOVE this import: import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Product", description = "Product management endpoints for vendors")
@SecurityRequirement(name = "bearerAuth")
public class ProductController {

    private final ProductService productService;

    // ============================================================
    // 1. POST / - Create Product
    // ============================================================

    @Operation(
            summary = "Create a new product",
            description = "Creates a new product for the authenticated vendor. Max 10 products per vendor. " +
                    "Image must be base64 encoded and less than 1MB. " +
                    "For GAS products: provide pricePerKg. For REGULAR products: provide price."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Product created successfully",
                    content = @Content(
                            examples = {
                                    @ExampleObject(
                                            name = "Gas Product",
                                            value = """
                                            {
                                                "success": true,
                                                "message": "Product created successfully",
                                                "data": {
                                                    "id": "550e8400-e29b-41d4-a716-446655440000",
                                                    "vendorId": "550e8400-e29b-41d4-a716-446655440000",
                                                    "name": "Premium Cooking Gas",
                                                    "description": "High quality cooking gas",
                                                    "isGas": true,
                                                    "pricePerKg": 850.00,
                                                    "displayPrice": "₦850.00/kg",
                                                    "weightKg": 12.5,
                                                    "unit": "kg",
                                                    "stockQuantity": 50,
                                                    "isActive": true
                                                },
                                                "timestamp": "2026-08-05T10:00:00Z"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Regular Product",
                                            value = """
                                            {
                                                "success": true,
                                                "message": "Product created successfully",
                                                "data": {
                                                    "id": "550e8400-e29b-41d4-a716-446655440001",
                                                    "vendorId": "550e8400-e29b-41d4-a716-446655440000",
                                                    "name": "Gas Cooker",
                                                    "description": "4-burner gas cooker",
                                                    "isGas": false,
                                                    "price": 50000.00,
                                                    "displayPrice": "₦50000.00/piece",
                                                    "unit": "piece",
                                                    "stockQuantity": 10,
                                                    "isActive": true
                                                },
                                                "timestamp": "2026-08-05T10:00:00Z"
                                            }
                                            """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Invalid data or max products reached"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Vendor not verified"
            )
    })
    @PostMapping
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Parameter(hidden = true) Authentication auth,
            @Valid @RequestBody CreateProductRequest request) throws BusinessException {
        UUID vendorId = UUID.fromString(auth.getName());
        log.info("📦 Vendor {} creating product: {} (Type: {})",
                vendorId, request.getName(), request.isGas() ? "GAS" : "REGULAR");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Product created successfully",
                        productService.createProduct(vendorId, request)));
    }

    // ============================================================
    // 2. PUT /{id} - Update Product
    // ============================================================

    @Operation(
            summary = "Update a product",
            description = "Updates an existing product. Only the vendor who created the product can update it."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Product updated successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Product not found"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Not your product"
            )
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @Parameter(hidden = true) Authentication auth,
            @Parameter(description = "Product ID", required = true) @PathVariable UUID id,
            @Valid @RequestBody UpdateProductRequest request) throws BusinessException {
        UUID vendorId = UUID.fromString(auth.getName());
        log.info("📦 Vendor {} updating product: {}", vendorId, id);
        return ResponseEntity.ok(ApiResponse.ok("Product updated successfully",
                productService.updateProduct(vendorId, id, request)));
    }

    // ============================================================
    // 3. DELETE /{id} - Delete Product (Soft Delete)
    // ============================================================

    @Operation(
            summary = "Delete a product",
            description = "Soft deletes a product (sets active to false). Only the vendor who created the product can delete it."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Product deleted successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Product not found"
            )
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @Parameter(hidden = true) Authentication auth,
            @Parameter(description = "Product ID", required = true) @PathVariable UUID id) throws BusinessException {
        UUID vendorId = UUID.fromString(auth.getName());
        log.info("🗑️ Vendor {} deleting product: {}", vendorId, id);
        productService.deleteProduct(vendorId, id);
        return ResponseEntity.ok(ApiResponse.ok("Product deleted successfully", null));
    }

    // ============================================================
    // 4. GET / - Get All Vendor Products
    // ============================================================

    @Operation(
            summary = "Get all vendor products",
            description = "Retrieves all products for the authenticated vendor with pagination."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Products retrieved successfully"
            )
    })
    @GetMapping
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getVendorProducts(
            @Parameter(hidden = true) Authentication auth,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "DESC") String direction) {
        UUID vendorId = UUID.fromString(auth.getName());
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.fromString(direction), sortBy);
        return ResponseEntity.ok(ApiResponse.ok(productService.getVendorProducts(vendorId, pageable)));
    }

    // ============================================================
    // 5. GET /active/{vendorId} - Get Active Products (Public)
    // ============================================================

    @Operation(
            summary = "Get active products for a vendor (public)",
            description = "Retrieves all active products for a specific vendor. This endpoint is public."
    )
    @GetMapping("/active/{vendorId}")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getActiveVendorProducts(
            @Parameter(description = "Vendor ID", required = true) @PathVariable UUID vendorId) {
        return ResponseEntity.ok(ApiResponse.ok(productService.getActiveVendorProducts(vendorId)));
    }

    // ============================================================
    // 6. GET /gas/{vendorId} - Get Gas Products (Public)
    // ============================================================

    @Operation(
            summary = "Get gas products for a vendor",
            description = "Retrieves all gas products (price per kg) for a specific vendor. This endpoint is public."
    )
    @GetMapping("/gas/{vendorId}")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getVendorGasProducts(
            @Parameter(description = "Vendor ID", required = true) @PathVariable UUID vendorId) {
        return ResponseEntity.ok(ApiResponse.ok(productService.getVendorGasProducts(vendorId)));
    }

    // ============================================================
    // 7. GET /{id} - Get Single Product
    // ============================================================

    @Operation(
            summary = "Get product by ID",
            description = "Retrieves a single product by its ID."
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(
            @Parameter(description = "Product ID", required = true) @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(productService.getProduct(id)));
    }

    // ============================================================
    // 8. GET /statistics - Get Product Statistics
    // ============================================================

    @Operation(
            summary = "Get product statistics",
            description = "Retrieves product statistics for the authenticated vendor's dashboard."
    )
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<ProductStatisticsDto>> getProductStatistics(
            @Parameter(hidden = true) Authentication auth) {
        UUID vendorId = UUID.fromString(auth.getName());
        return ResponseEntity.ok(ApiResponse.ok(productService.getProductStatistics(vendorId)));
    }

    // ============================================================
    // 9. GET /low-stock - Get Low Stock Products
    // ============================================================

    @Operation(
            summary = "Get low stock products",
            description = "Retrieves products with stock below the specified threshold."
    )
    @GetMapping("/low-stock")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getLowStockProducts(
            @Parameter(hidden = true) Authentication auth,
            @Parameter(description = "Stock threshold") @RequestParam(defaultValue = "10") int threshold) {
        UUID vendorId = UUID.fromString(auth.getName());
        return ResponseEntity.ok(ApiResponse.ok(productService.getLowStockProducts(vendorId, threshold)));
    }

    // ============================================================
    // 10. PATCH /{id}/toggle - Toggle Product Status
    // ============================================================

    @Operation(
            summary = "Toggle product active status",
            description = "Toggles the active status of a product (activate/deactivate)."
    )
    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<ProductResponse>> toggleProductStatus(
            @Parameter(hidden = true) Authentication auth,
            @Parameter(description = "Product ID", required = true) @PathVariable UUID id) throws BusinessException {
        UUID vendorId = UUID.fromString(auth.getName());
        log.info("🔄 Vendor {} toggling product status: {}", vendorId, id);
        return ResponseEntity.ok(ApiResponse.ok("Product status toggled",
                productService.toggleProductStatus(vendorId, id)));
    }

    // ============================================================
    // 11. GET /remaining-slots - Get Remaining Product Slots
    // ============================================================

    @Operation(
            summary = "Get remaining product slots",
            description = "Returns the number of remaining product slots available for the vendor (max 10)."
    )
    @GetMapping("/remaining-slots")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<Integer>> getRemainingSlots(
            @Parameter(hidden = true) Authentication auth) {
        UUID vendorId = UUID.fromString(auth.getName());
        int remainingSlots = productService.getRemainingProductSlots(vendorId);
        return ResponseEntity.ok(ApiResponse.ok(remainingSlots));
    }

    // ============================================================
    // 12. GET /can-add - Check If Can Add More Products
    // ============================================================

    @Operation(
            summary = "Check if vendor can add more products",
            description = "Returns true if the vendor can add more products (less than 10 products)."
    )
    @GetMapping("/can-add")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<Boolean>> canAddMoreProducts(
            @Parameter(hidden = true) Authentication auth) {
        UUID vendorId = UUID.fromString(auth.getName());
        boolean canAdd = productService.canAddMoreProducts(vendorId);
        return ResponseEntity.ok(ApiResponse.ok(canAdd));
    }
}