package com.gaslink.api.modules.order;

import com.gaslink.api.exception.BusinessException;
import com.gaslink.api.modules.order.dto.*;
import com.gaslink.api.modules.order.service.OrderService;
import com.gaslink.api.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Order", description = "Order management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;

    // ============================================================
    // 1. POST /gas - Create Gas Order (Customer)
    // ============================================================

    @PostMapping("/gas")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<OrderResponse>> createGasOrder(
            @Parameter(hidden = true) Authentication auth,
            @Valid @RequestBody CreateGasOrderRequest request) {
        UUID customerId = UUID.fromString(auth.getName());
        log.info("🆕 Customer {} creating gas order: {}kg", customerId, request.getQuantityKg());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Gas order created successfully",
                        orderService.createGasOrder(customerId, request)));
    }

    // ============================================================
    // 2. POST /nearby - Get Nearby Gas Orders for Bidding (Vendor)
    // ============================================================

    @PostMapping("/nearby")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<List<NearbyOrderResponse>>> getNearbyOrdersForBidding(
            @Parameter(hidden = true) Authentication auth,
            @Valid @RequestBody NearbyOrderRequest request) throws BusinessException {
        UUID vendorId = UUID.fromString(auth.getName());
        return ResponseEntity.ok(ApiResponse.ok(
                orderService.getNearbyOrdersForBidding(vendorId, request)));
    }

    // ============================================================
    // 3. POST /nearby/regular - Get Nearby Regular Orders (Vendor)
    // ============================================================

    @PostMapping("/nearby/regular")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<List<NearbyOrderResponse>>> getNearbyOrders(
            @Parameter(hidden = true) Authentication auth,
            @Valid @RequestBody NearbyOrderRequest request) {
        UUID vendorId = UUID.fromString(auth.getName());
        return ResponseEntity.ok(ApiResponse.ok(
                orderService.getNearbyPendingOrders(vendorId, request)));
    }

    // ============================================================
    // 4. POST /{id}/bid - Submit Bid (Vendor)
    // ============================================================

    @PostMapping("/{id}/bid")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<BidResponse>> submitBid(
            @Parameter(hidden = true) Authentication auth,
            @Parameter(description = "Order ID", required = true) @PathVariable UUID id,
            @Valid @RequestBody SubmitBidRequest request) throws BusinessException {
        UUID vendorId = UUID.fromString(auth.getName());
        log.info("💬 Vendor {} submitting bid for order: {}", vendorId, id);
        return ResponseEntity.ok(ApiResponse.ok("Bid submitted successfully",
                orderService.submitBid(vendorId, id, request)));
    }

    // ============================================================
    // 5. GET /{id}/bids - Get All Bids for Order (Customer)
    // ============================================================

    @GetMapping("/{id}/bids")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<List<BidResponse>>> getBidsForOrder(
            @Parameter(hidden = true) Authentication auth,
            @Parameter(description = "Order ID", required = true) @PathVariable UUID id) throws BusinessException {
        UUID customerId = UUID.fromString(auth.getName());
        return ResponseEntity.ok(ApiResponse.ok(
                orderService.getBidsForOrder(customerId, id)));
    }

    // ============================================================
    // 6. POST /{orderId}/bids/{bidId}/approve - Approve Bid (Customer)
    // ============================================================

    @PostMapping("/{orderId}/bids/{bidId}/approve")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<OrderResponse>> approveBid(
            @Parameter(hidden = true) Authentication auth,
            @Parameter(description = "Order ID", required = true) @PathVariable UUID orderId,
            @Parameter(description = "Bid ID", required = true) @PathVariable UUID bidId) throws BusinessException {
        UUID customerId = UUID.fromString(auth.getName());
        log.info("✅ Customer {} approving bid {} for order {}", customerId, bidId, orderId);
        return ResponseEntity.ok(ApiResponse.ok("Bid approved successfully",
                orderService.approveBid(customerId, orderId, bidId)));
    }

    // ============================================================
    // 7. POST / - Create Regular Order (Customer)
    // ============================================================

    @Operation(
            summary = "Create a new regular order",
            description = "Customer creates a new order for regular products (non-gas)."
    )
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Parameter(hidden = true) Authentication auth,
            @Valid @RequestBody CreateOrderRequest request) throws BusinessException {
        UUID customerId = UUID.fromString(auth.getName());
        log.info("🆕 Customer {} creating regular order for vendor: {}", customerId, request.getVendorId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Order created successfully",
                        orderService.createOrder(customerId, request)));
    }

    // ============================================================
    // 8. POST /{id}/accept - Accept Order (Vendor - Regular Orders)
    // ============================================================

    @PostMapping("/{id}/accept")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<OrderResponse>> acceptOrder(
            @Parameter(hidden = true) Authentication auth,
            @Parameter(description = "Order ID", required = true) @PathVariable UUID id) throws BusinessException {
        UUID vendorId = UUID.fromString(auth.getName());
        log.info("✅ Vendor {} accepting order: {}", vendorId, id);
        return ResponseEntity.ok(ApiResponse.ok("Order accepted successfully",
                orderService.acceptOrder(vendorId, id)));
    }

    // ============================================================
    // 9. POST /{id}/reject - Reject Order (Vendor)
    // ============================================================

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<OrderResponse>> rejectOrder(
            @Parameter(hidden = true) Authentication auth,
            @Parameter(description = "Order ID", required = true) @PathVariable UUID id,
            @RequestParam(required = false) String reason) throws BusinessException {
        UUID vendorId = UUID.fromString(auth.getName());
        log.info("❌ Vendor {} rejecting order: {}", vendorId, id);
        return ResponseEntity.ok(ApiResponse.ok("Order rejected",
                orderService.rejectOrder(vendorId, id, reason)));
    }

    // ============================================================
    // 10. GET /vendor - Get Vendor Orders
    // ============================================================

    @GetMapping("/vendor")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getVendorOrders(
            @Parameter(hidden = true) Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        UUID vendorId = UUID.fromString(auth.getName());
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.fromString(direction), sortBy);
        return ResponseEntity.ok(ApiResponse.ok(
                orderService.getVendorOrders(vendorId, pageable)));
    }

    // ============================================================
    // 11. GET /customer - Get Customer Orders
    // ============================================================

    @GetMapping("/customer")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getCustomerOrders(
            @Parameter(hidden = true) Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        UUID customerId = UUID.fromString(auth.getName());
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.fromString(direction), sortBy);
        return ResponseEntity.ok(ApiResponse.ok(
                orderService.getCustomerOrders(customerId, pageable)));
    }

    // ============================================================
    // 12. GET /{id} - Get Single Order
    // ============================================================

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(
            @Parameter(description = "Order ID", required = true) @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getOrder(id)));
    }

    // ============================================================
    // 13. POST /{id}/cancel - Cancel Order (Customer)
    // ============================================================

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @Parameter(hidden = true) Authentication auth,
            @Parameter(description = "Order ID", required = true) @PathVariable UUID id,
            @RequestParam(required = false) String reason) throws BusinessException {
        UUID customerId = UUID.fromString(auth.getName());
        log.info("🔄 Customer {} cancelling order: {}", customerId, id);
        return ResponseEntity.ok(ApiResponse.ok("Order cancelled",
                orderService.cancelOrder(customerId, id, reason)));
    }

    // ============================================================
    // 14. PATCH /{id}/status - Update Order Status (Vendor)
    // ============================================================

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @Parameter(hidden = true) Authentication auth,
            @Parameter(description = "Order ID", required = true) @PathVariable UUID id,
            @Valid @RequestBody OrderStatusUpdateRequest request) throws BusinessException {
        UUID vendorId = UUID.fromString(auth.getName());
        log.info("📦 Vendor {} updating order {} status to: {}", vendorId, id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.ok("Order status updated",
                orderService.updateOrderStatus(vendorId, id, request.getStatus())));
    }

    // ============================================================
    // 15. GET /statistics/vendor - Vendor Order Statistics
    // ============================================================

    @GetMapping("/statistics/vendor")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<OrderStatisticsDto>> getVendorOrderStats(
            @Parameter(hidden = true) Authentication auth) {
        UUID vendorId = UUID.fromString(auth.getName());
        return ResponseEntity.ok(ApiResponse.ok(
                orderService.getVendorOrderStats(vendorId)));
    }
}