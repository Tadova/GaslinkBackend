package com.gaslink.api.modules.order.service;

import com.gaslink.api.exception.BusinessException;
import com.gaslink.api.modules.order.Order;
import com.gaslink.api.modules.order.OrderBid;
import com.gaslink.api.modules.order.OrderBidRepository;
import com.gaslink.api.modules.order.OrderRepository;
import com.gaslink.api.modules.order.dto.*;
import com.gaslink.api.modules.product.Product;
import com.gaslink.api.modules.product.ProductRepository;
import com.gaslink.api.modules.user.User;
import com.gaslink.api.modules.user.UserRepository;
import com.gaslink.api.modules.vendor.Vendor;
import com.gaslink.api.modules.vendor.VendorRepository;
import com.gaslink.api.shared.enums.BidStatus;
import com.gaslink.api.shared.enums.OrderStatus;
import com.gaslink.api.shared.enums.PaymentMethod;
import com.gaslink.api.shared.enums.PaymentStatus;
import com.gaslink.api.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderBidRepository orderBidRepository;
    private final VendorRepository vendorRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final int MAX_BID_DISTANCE_KM = 20;
    private static final int DEFAULT_BID_DEADLINE_MINUTES = 30;
    private static final int BID_EXPIRY_MINUTES = 15;

    /**
     * Calculate distance between two coordinates using Haversine formula
     */
    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    /**
     * Generate unique order reference
     */
    private String generateOrderReference() {
        return "GL-" + System.currentTimeMillis() + "-" +
                UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    // ============================================================
    // 1. CUSTOMER - Create Regular Order (with products)
    // ============================================================

    @Transactional
    public OrderResponse createOrder(UUID customerId, CreateOrderRequest request) throws BusinessException {
        // Validate customer exists
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        // Validate vendor exists and is verified
        Vendor vendor = vendorRepository.findById(request.getVendorId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

        if (vendor.getVerificationStatus() != com.gaslink.api.shared.enums.VerificationStatus.VERIFIED) {
            throw new BusinessException("Vendor is not verified");
        }

        // Calculate distance
        double distance = calculateDistance(
                request.getCustomerLat(), request.getCustomerLng(),
                vendor.getLat(), vendor.getLng()
        );

        // Create order
        Order order = Order.builder()
                .customerId(customerId)
                .vendorId(request.getVendorId())
                .orderReference(generateOrderReference())
                .status(OrderStatus.PENDING)
                .isGasOrder(false)
                .customerLat(request.getCustomerLat())
                .customerLng(request.getCustomerLng())
                .deliveryAddress(request.getDeliveryAddress())
                .deliveryNotes(request.getDeliveryNotes())
                .distanceKm(distance)
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderBid> orderBids = new ArrayList<>();

        // Process product items
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (OrderItemRequest itemRequest : request.getItems()) {
                Product product = productRepository.findById(itemRequest.getProductId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

                if (!product.getVendorId().equals(vendor.getId())) {
                    throw new BusinessException("Product does not belong to this vendor");
                }

                if (!product.isActive()) {
                    throw new BusinessException("Product is not available: " + product.getName());
                }

                if (product.getStockQuantity() < itemRequest.getQuantity()) {
                    throw new BusinessException("Insufficient stock for product: " + product.getName());
                }

                BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
                totalAmount = totalAmount.add(subtotal);

                // Reduce stock
                product.setStockQuantity(product.getStockQuantity() - itemRequest.getQuantity());
                productRepository.save(product);
            }
        }

        // Calculate delivery fee
        BigDecimal deliveryFee = calculateDeliveryFee(distance);
        BigDecimal finalAmount = totalAmount.add(deliveryFee);

        order.setTotalAmount(totalAmount);
        order.setDeliveryFee(deliveryFee);
        order.setDiscount(BigDecimal.ZERO);
        order.setFinalAmount(finalAmount);
        order.setPaymentMethod(PaymentMethod.CARD);
        order.setPaymentStatus(PaymentStatus.PENDING);

        Order savedOrder = orderRepository.save(order);

        log.info("🆕 Order created: {} by customer: {}", savedOrder.getOrderReference(), customerId);

        return toOrderResponse(savedOrder);
    }

    /**
     * Calculate delivery fee based on distance
     */
    private BigDecimal calculateDeliveryFee(double distance) {
        if (distance <= 2) {
            return new BigDecimal("500.00");
        } else if (distance <= 5) {
            return new BigDecimal("1000.00");
        } else if (distance <= 10) {
            return new BigDecimal("1500.00");
        } else if (distance <= 20) {
            return new BigDecimal("2000.00");
        } else {
            return new BigDecimal("2500.00");
        }
    }

    // ============================================================
    // 2. CUSTOMER - Create Gas Order
    // ============================================================

    @Transactional
    public OrderResponse createGasOrder(UUID customerId, CreateGasOrderRequest request) {
        // Validate customer exists
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        // Create order
        Instant now = Instant.now();
        Instant bidDeadline = now.plus(request.getBidDeadlineMinutes() != null ?
                request.getBidDeadlineMinutes() : DEFAULT_BID_DEADLINE_MINUTES, ChronoUnit.MINUTES);

        Order order = Order.builder()
                .customerId(customerId)
                .orderReference(generateOrderReference())
                .status(OrderStatus.PENDING)
                .isGasOrder(true)
                .gasQuantityKg(request.getQuantityKg())
                .customerLat(request.getCustomerLat())
                .customerLng(request.getCustomerLng())
                .deliveryAddress(request.getDeliveryAddress())
                .deliveryNotes(request.getDeliveryNotes())
                .bidDeadline(bidDeadline)
                .bids(new ArrayList<>())
                .build();

        Order savedOrder = orderRepository.save(order);

        log.info("🆕 Gas order created: {} by customer: {} - {}kg",
                savedOrder.getOrderReference(), customerId, request.getQuantityKg());

        // Notify nearby vendors about new order
        notifyNearbyVendorsAboutOrder(savedOrder);

        return toOrderResponse(savedOrder);
    }

    /**
     * Notify nearby vendors about new order
     */
    private void notifyNearbyVendorsAboutOrder(Order order) {
        List<Vendor> vendors = vendorRepository.findByVerificationStatusAndSubscriptionStatus(
                com.gaslink.api.shared.enums.VerificationStatus.VERIFIED, "ACTIVE");

        int notifiedCount = 0;
        for (Vendor vendor : vendors) {
            double distance = calculateDistance(
                    order.getCustomerLat(), order.getCustomerLng(),
                    vendor.getLat(), vendor.getLng()
            );

            if (distance <= MAX_BID_DISTANCE_KM) {
                sendPushNotificationToVendor(vendor, order, distance);
                notifiedCount++;
            }
        }

        log.info("📱 Notified {} vendors about order: {}", notifiedCount, order.getOrderReference());
    }

    // ============================================================
    // 3. VENDOR - Get Nearby Orders for Bidding (Gas Orders Only)
    // ============================================================

    public List<NearbyOrderResponse> getNearbyOrdersForBidding(UUID vendorId, NearbyOrderRequest request) throws BusinessException {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

        if (vendor.getVerificationStatus() != com.gaslink.api.shared.enums.VerificationStatus.VERIFIED) {
            throw new BusinessException("Vendor must be verified to bid on orders");
        }

        if (!"ACTIVE".equals(vendor.getSubscriptionStatus())) {
            throw new BusinessException("Vendor subscription is not active");
        }

        List<Order> pendingOrders = orderRepository.findByStatus(OrderStatus.PENDING);
        List<NearbyOrderResponse> nearbyOrders = new ArrayList<>();

        for (Order order : pendingOrders) {
            if (!order.isGasOrder()) continue;
            if (order.getSelectedBidId() != null) continue;

            boolean alreadyBid = orderBidRepository.existsByOrderIdAndVendorId(order.getId(), vendorId);
            if (alreadyBid) continue;

            double distance = calculateDistance(
                    request.getVendorLat(), request.getVendorLng(),
                    order.getCustomerLat(), order.getCustomerLng()
            );

            double radius = request.getRadiusKm() != null ? request.getRadiusKm() : MAX_BID_DISTANCE_KM;
            if (distance <= radius) {
                User customer = userRepository.findById(order.getCustomerId()).orElse(null);

                NearbyOrderResponse response = NearbyOrderResponse.builder()
                        .orderId(order.getId())
                        .orderReference(order.getOrderReference()) // Now this works
                        .customerId(order.getCustomerId())
                        .customerName(customer != null ? customer.getFullName() : "Unknown")
                        .customerPhone(customer != null ? customer.getPhone() : "Unknown")
                        .customerLat(order.getCustomerLat())
                        .customerLng(order.getCustomerLng())
                        .gasQuantityKg(order.getGasQuantityKg())
                        .deliveryAddress(order.getDeliveryAddress())
                        .deliveryNotes(order.getDeliveryNotes())
                        .distanceKm(distance)
                        .bidDeadline(order.getBidDeadline())
                        .createdAt(order.getCreatedAt())
                        .totalBids(orderBidRepository.countByOrderId(order.getId()))
                        .build();

                nearbyOrders.add(response);
            }
        }

        return nearbyOrders;
    }

    // ============================================================
    // 4. VENDOR - Get Nearby Pending Orders (Legacy - for regular orders)
    // ============================================================

    public List<NearbyOrderResponse> getNearbyPendingOrders(UUID vendorId, NearbyOrderRequest request) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

        List<Order> pendingOrders = orderRepository.findByStatus(OrderStatus.PENDING);
        List<NearbyOrderResponse> nearbyOrders = new ArrayList<>();

        for (Order order : pendingOrders) {
            // Skip gas orders (they go through bidding)
            if (order.isGasOrder()) continue;

            double distance = calculateDistance(
                    request.getVendorLat(), request.getVendorLng(),
                    order.getCustomerLat(), order.getCustomerLng()
            );

            double radius = request.getRadiusKm() != null ? request.getRadiusKm() : MAX_BID_DISTANCE_KM;
            if (distance <= radius) {
                User customer = userRepository.findById(order.getCustomerId()).orElse(null);

                NearbyOrderResponse response = NearbyOrderResponse.builder()
                        .orderId(order.getId())
                        .orderReference(order.getOrderReference()) // Now this works
                        .customerId(order.getCustomerId())
                        .customerName(customer != null ? customer.getFullName() : "Unknown")
                        .customerPhone(customer != null ? customer.getPhone() : "Unknown")
                        .customerLat(order.getCustomerLat())
                        .customerLng(order.getCustomerLng())
                        .totalAmount(order.getFinalAmount())
                        .isGasOrder(false)
                        .distanceKm(distance)
                        .createdAt(order.getCreatedAt())
                        .build();

                nearbyOrders.add(response);
            }
        }

        return nearbyOrders;
    }

    // ============================================================
    // 5. VENDOR - Submit Bid on Gas Order
    // ============================================================

    @Transactional
    public BidResponse submitBid(UUID vendorId, UUID orderId, SubmitBidRequest request) throws BusinessException {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

        if (vendor.getVerificationStatus() != com.gaslink.api.shared.enums.VerificationStatus.VERIFIED) {
            throw new BusinessException("Vendor must be verified to bid");
        }

        if (!"ACTIVE".equals(vendor.getSubscriptionStatus())) {
            throw new BusinessException("Vendor subscription is not active");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException("Order is no longer accepting bids");
        }

        if (!order.isGasOrder()) {
            throw new BusinessException("This order is not a gas order");
        }

        if (order.getBidDeadline() != null && Instant.now().isAfter(order.getBidDeadline())) {
            throw new BusinessException("Bidding deadline has passed");
        }

        if (orderBidRepository.existsByOrderIdAndVendorId(orderId, vendorId)) {
            throw new BusinessException("You have already submitted a bid for this order");
        }

        if (order.getSelectedBidId() != null) {
            throw new BusinessException("This order has already been assigned to another vendor");
        }

        double distance = calculateDistance(
                order.getCustomerLat(), order.getCustomerLng(),
                vendor.getLat(), vendor.getLng()
        );

        if (distance > MAX_BID_DISTANCE_KM) {
            throw new BusinessException("You are too far from this customer to bid");
        }

        BigDecimal totalAmount = request.getPricePerKg()
                .multiply(BigDecimal.valueOf(order.getGasQuantityKg()))
                .add(request.getDeliveryFee());

        OrderBid bid = OrderBid.builder()
                .orderId(orderId)
                .vendorId(vendorId)
                .vendorName(vendor.getBusinessName())
                .pricePerKg(request.getPricePerKg())
                .deliveryFee(request.getDeliveryFee())
                .totalAmount(totalAmount)
                .estimatedDeliveryTime(request.getEstimatedDeliveryTime() != null ?
                        request.getEstimatedDeliveryTime() : 30)
                .deliveryNotes(request.getDeliveryNotes())
                .status(BidStatus.PENDING)
                .expiresAt(Instant.now().plus(BID_EXPIRY_MINUTES, ChronoUnit.MINUTES))
                .distanceKm(distance)
                .vendorLat(vendor.getLat())
                .vendorLng(vendor.getLng())
                .build();

        OrderBid savedBid = orderBidRepository.save(bid);

        log.info("💬 Vendor {} submitted bid for order {}: ₦{}/kg",
                vendorId, orderId, request.getPricePerKg());

        notifyCustomerAboutNewBid(order, vendor, savedBid);

        return toBidResponse(savedBid);
    }

    // ============================================================
    // 6. CUSTOMER - View All Bids for Their Order
    // ============================================================

    public List<BidResponse> getBidsForOrder(UUID customerId, UUID orderId) throws BusinessException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getCustomerId().equals(customerId)) {
            throw new BusinessException("You don't have permission to view bids for this order");
        }

        List<OrderBid> bids = orderBidRepository.findByOrderIdAndStatusOrderByPricePerKgAsc(
                orderId, BidStatus.PENDING);

        return bids.stream()
                .map(this::toBidResponse)
                .collect(Collectors.toList());
    }

    // ============================================================
    // 7. CUSTOMER - Approve a Bid
    // ============================================================

    @Transactional
    public OrderResponse approveBid(UUID customerId, UUID orderId, UUID bidId) throws BusinessException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getCustomerId().equals(customerId)) {
            throw new BusinessException("You don't have permission to approve bids for this order");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException("This order is no longer active");
        }

        OrderBid bid = orderBidRepository.findById(bidId)
                .orElseThrow(() -> new ResourceNotFoundException("Bid not found"));

        if (bid.getStatus() != BidStatus.PENDING) {
            throw new BusinessException("This bid is no longer available");
        }

        if (bid.getExpiresAt() != null && Instant.now().isAfter(bid.getExpiresAt())) {
            bid.setStatus(BidStatus.EXPIRED);
            orderBidRepository.save(bid);
            throw new BusinessException("This bid has expired");
        }

        if (order.getSelectedBidId() != null) {
            throw new BusinessException("This order has already been assigned to another vendor");
        }

        // Reject all other pending bids
        List<OrderBid> otherBids = orderBidRepository.findByOrderIdAndStatus(orderId, BidStatus.PENDING);
        for (OrderBid otherBid : otherBids) {
            if (!otherBid.getId().equals(bidId)) {
                otherBid.setStatus(BidStatus.REJECTED);
                orderBidRepository.save(otherBid);
            }
        }

        // Approve the selected bid
        bid.setStatus(BidStatus.APPROVED);
        orderBidRepository.save(bid);

        // Update order with selected bid
        order.setSelectedBidId(bidId);
        order.setVendorId(bid.getVendorId());
        order.setApprovedPricePerKg(bid.getPricePerKg());
        order.setApprovedDeliveryFee(bid.getDeliveryFee());
        order.setApprovedTotalAmount(bid.getTotalAmount());
        order.setApprovedAt(Instant.now());
        order.setStatus(OrderStatus.ACCEPTED);

        Order updatedOrder = orderRepository.save(order);

        log.info("✅ Customer {} approved bid {} for order {}", customerId, bidId, orderId);

        notifyVendorsAboutBidResult(updatedOrder, bid);

        return toOrderResponse(updatedOrder);
    }

    // ============================================================
    // 8. VENDOR - Accept Order (For regular orders - first come first serve)
    // ============================================================

    @Transactional
    public OrderResponse acceptOrder(UUID vendorId, UUID orderId) throws BusinessException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getVendorId().equals(vendorId)) {
            throw new BusinessException("You don't have permission to accept this order");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException("Order is no longer pending");
        }

        // Check if it's a gas order (should go through bidding)
        if (order.isGasOrder()) {
            throw new BusinessException("Gas orders must go through the bidding process");
        }

        order.setStatus(OrderStatus.ACCEPTED);
        order.setAcceptedAt(Instant.now());

        Order acceptedOrder = orderRepository.save(order);

        log.info("✅ Order accepted: {} by vendor: {}", order.getOrderReference(), vendorId);

        notifyCustomerOrderAccepted(acceptedOrder);

        return toOrderResponse(acceptedOrder);
    }

    // ============================================================
    // 9. VENDOR - Reject Order
    // ============================================================

    @Transactional
    public OrderResponse rejectOrder(UUID vendorId, UUID orderId, String reason) throws BusinessException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getVendorId().equals(vendorId)) {
            throw new BusinessException("You don't have permission to reject this order");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException("Order is no longer pending");
        }

        order.setStatus(OrderStatus.REJECTED);
        order.setRejectedAt(Instant.now());
        order.setRejectionReason(reason != null ? reason : "Vendor rejected the order");

        Order rejectedOrder = orderRepository.save(order);

        log.info("❌ Order rejected: {} by vendor: {}", order.getOrderReference(), vendorId);

        notifyCustomerOrderRejected(rejectedOrder);

        return toOrderResponse(rejectedOrder);
    }

    // ============================================================
    // 10. GET - Get Order Details
    // ============================================================

    public OrderResponse getOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return toOrderResponse(order);
    }

    // ============================================================
    // 11. VENDOR - Update Order Status
    // ============================================================

    @Transactional
    public OrderResponse updateOrderStatus(UUID vendorId, UUID orderId, String status) throws BusinessException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getVendorId().equals(vendorId)) {
            throw new BusinessException("You don't have permission to update this order");
        }

        OrderStatus newStatus = OrderStatus.valueOf(status.toUpperCase());

        if (order.getStatus() == OrderStatus.REJECTED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessException("Cannot update a rejected or cancelled order");
        }

        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new BusinessException("Order is already completed");
        }

        switch (newStatus) {
            case PROCESSING:
                if (order.getStatus() != OrderStatus.ACCEPTED) {
                    throw new BusinessException("Order must be accepted before processing");
                }
                break;
            case READY:
                if (order.getStatus() != OrderStatus.PROCESSING) {
                    throw new BusinessException("Order must be processing before it can be marked as ready");
                }
                break;
            case COMPLETED:
                if (order.getStatus() != OrderStatus.READY) {
                    throw new BusinessException("Order must be ready before completion");
                }
                order.setCompletedAt(Instant.now());
                break;
            default:
                throw new BusinessException("Invalid status transition");
        }

        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);

        log.info("📦 Order status updated: {} - {}", order.getOrderReference(), newStatus);

        notifyCustomerStatusUpdate(updatedOrder);

        return toOrderResponse(updatedOrder);
    }

    // ============================================================
    // 12. CUSTOMER - Cancel Order
    // ============================================================

    @Transactional
    public OrderResponse cancelOrder(UUID customerId, UUID orderId, String reason) throws BusinessException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getCustomerId().equals(customerId)) {
            throw new BusinessException("You don't have permission to cancel this order");
        }

        // For gas orders, check if a bid has been approved
        if (order.isGasOrder() && order.getSelectedBidId() != null) {
            throw new BusinessException("Cannot cancel order after a bid has been approved");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException("Cannot cancel order. Current status: " + order.getStatus());
        }

        // Cancel all pending bids for gas orders
        if (order.isGasOrder()) {
            List<OrderBid> pendingBids = orderBidRepository.findByOrderIdAndStatus(orderId, BidStatus.PENDING);
            for (OrderBid bid : pendingBids) {
                bid.setStatus(BidStatus.REJECTED);
                orderBidRepository.save(bid);
            }
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(Instant.now());
        order.setCancellationReason(reason != null ? reason : "Customer cancelled the order");

        Order cancelledOrder = orderRepository.save(order);

        log.info("🔄 Order cancelled: {} by customer: {}", order.getOrderReference(), customerId);

        notifyVendorsAboutCancellation(cancelledOrder);

        return toOrderResponse(cancelledOrder);
    }

    // ============================================================
    // 13. GET - Vendor Orders
    // ============================================================

    public Page<OrderResponse> getVendorOrders(UUID vendorId, Pageable pageable) {
        return orderRepository.findByVendorId(vendorId, pageable)
                .map(this::toOrderResponse);
    }

    // ============================================================
    // 14. GET - Customer Orders
    // ============================================================

    public Page<OrderResponse> getCustomerOrders(UUID customerId, Pageable pageable) {
        return orderRepository.findByCustomerId(customerId, pageable)
                .map(this::toOrderResponse);
    }

    // ============================================================
    // 15. GET - Vendor Order Statistics
    // ============================================================

    public OrderStatisticsDto getVendorOrderStats(UUID vendorId) {
        long pending = orderRepository.countByVendorIdAndStatus(vendorId, OrderStatus.PENDING);
        long accepted = orderRepository.countByVendorIdAndStatus(vendorId, OrderStatus.ACCEPTED);
        long processing = orderRepository.countByVendorIdAndStatus(vendorId, OrderStatus.PROCESSING);
        long ready = orderRepository.countByVendorIdAndStatus(vendorId, OrderStatus.READY);
        long completed = orderRepository.countByVendorIdAndStatus(vendorId, OrderStatus.COMPLETED);
        long rejected = orderRepository.countByVendorIdAndStatus(vendorId, OrderStatus.REJECTED);
        long cancelled = orderRepository.countByVendorIdAndStatus(vendorId, OrderStatus.CANCELLED);

        List<Object[]> stats = orderRepository.getVendorOrderStats(vendorId);
        long totalOrders = stats.isEmpty() ? 0 : (Long) stats.get(0)[0];
        BigDecimal totalRevenue = stats.isEmpty() ? BigDecimal.ZERO : (BigDecimal) stats.get(0)[1];

        return OrderStatisticsDto.builder()
                .totalOrders(totalOrders)
                .pendingOrders(pending)
                .acceptedOrders(accepted)
                .processingOrders(processing)
                .readyOrders(ready)
                .completedOrders(completed)
                .rejectedOrders(rejected)
                .cancelledOrders(cancelled)
                .totalRevenue(totalRevenue)
                .build();
    }

    // ============================================================
    // 16. SCHEDULED - Auto-expire expired bids
    // ============================================================

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void expireExpiredBids() {
        Instant now = Instant.now();
        List<OrderBid> expiredBids = orderBidRepository.findByStatusAndExpiresAtBefore(BidStatus.PENDING, now);

        for (OrderBid bid : expiredBids) {
            bid.setStatus(BidStatus.EXPIRED);
            orderBidRepository.save(bid);
        }

        if (!expiredBids.isEmpty()) {
            log.info("⏰ Expired {} bids", expiredBids.size());
        }
    }

    // ============================================================
    // 17. SCHEDULED - Auto-expire orders with expired bid deadlines
    // ============================================================

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void expireOrdersWithNoBids() {
        Instant now = Instant.now();
        List<Order> orders = orderRepository.findByStatusAndBidDeadlineBefore(OrderStatus.PENDING, now);

        for (Order order : orders) {
            // Create a final copy of the order reference for the lambda
            UUID orderId = order.getId();
            String orderReference = order.getOrderReference();

            long pendingBidCount = orderBidRepository.countByOrderIdAndStatus(orderId, BidStatus.PENDING);

            if (pendingBidCount == 0) {
                order.setStatus(OrderStatus.CANCELLED);
                order.setCancelledAt(now);
                order.setCancellationReason("No bids received before deadline");
                orderRepository.save(order);
                log.info("⏰ Order expired (no bids): {}", orderReference);
            } else {
                log.info("⏰ Bidding deadline passed for order: {} - {} bids pending",
                        orderReference, pendingBidCount);
            }
        }
    }

    // ============================================================
    // NOTIFICATION METHODS
    // ============================================================

    private void sendPushNotificationToVendor(Vendor vendor, Order order, double distance) {
        log.info("📱 Notifying vendor {} about order {} ({}km away)",
                vendor.getId(), order.getOrderReference(), String.format("%.2f", distance));
    }

    private void notifyCustomerAboutNewBid(Order order, Vendor vendor, OrderBid bid) {
        log.info("📱 Notifying customer {} about new bid from vendor {} for order {}",
                order.getCustomerId(), vendor.getId(), order.getOrderReference());
    }

    private void notifyCustomerOrderAccepted(Order order) {
        log.info("📱 Notifying customer {} about accepted order: {}",
                order.getCustomerId(), order.getOrderReference());
    }

    private void notifyCustomerOrderRejected(Order order) {
        log.info("📱 Notifying customer {} about rejected order: {}",
                order.getCustomerId(), order.getOrderReference());
    }

    private void notifyVendorsAboutBidResult(Order order, OrderBid approvedBid) {
        log.info("📱 Notifying vendors about bid result for order {}", order.getOrderReference());
    }

    private void notifyCustomerStatusUpdate(Order order) {
        log.info("📱 Notifying customer {} about order status: {}",
                order.getCustomerId(), order.getStatus());
    }

    private void notifyVendorsAboutCancellation(Order order) {
        log.info("📱 Notifying vendors about order cancellation: {}", order.getOrderReference());
    }

    // ============================================================
    // DTO MAPPING METHODS
    // ============================================================

    private BidResponse toBidResponse(OrderBid bid) {
        Vendor vendor = vendorRepository.findById(bid.getVendorId()).orElse(null);

        return BidResponse.builder()
                .id(bid.getId())
                .orderId(bid.getOrderId())
                .vendorId(bid.getVendorId())
                .vendorName(bid.getVendorName())
                .businessName(vendor != null ? vendor.getBusinessName() : "Unknown")
                .vendorRating(vendor != null ? vendor.getRating().doubleValue() : 0.0)
                .pricePerKg(bid.getPricePerKg())
                .deliveryFee(bid.getDeliveryFee())
                .totalAmount(bid.getTotalAmount())
                .estimatedDeliveryTime(bid.getEstimatedDeliveryTime())
                .deliveryNotes(bid.getDeliveryNotes())
                .distanceKm(bid.getDistanceKm())
                .status(bid.getStatus())
                .expiresAt(bid.getExpiresAt())
                .createdAt(bid.getCreatedAt())
                .build();
    }

    private OrderResponse toOrderResponse(Order order) {
        // Get customer name - don't use lambda to modify variable
        String customerName = userRepository.findById(order.getCustomerId())
                .map(User::getFullName)
                .orElse("Unknown Customer");

        // Get vendor name - don't use lambda to modify variable
        String vendorName = null;
        if (order.getVendorId() != null) {
            vendorName = vendorRepository.findById(order.getVendorId())
                    .map(Vendor::getBusinessName)
                    .orElse(null);
        }

        List<BidResponse> bidResponses = new ArrayList<>();
        if (order.getBids() != null && !order.getBids().isEmpty()) {
            bidResponses = order.getBids().stream()
                    .map(this::toBidResponse)
                    .collect(Collectors.toList());
        }

        return OrderResponse.builder()
                .id(order.getId())
                .orderReference(order.getOrderReference())
                .customerId(order.getCustomerId())
                .customerName(customerName)
                .vendorId(order.getVendorId())
                .vendorName(vendorName)
                .status(order.getStatus())
                .isGasOrder(order.isGasOrder())
                .gasQuantityKg(order.getGasQuantityKg())
                .customerLat(order.getCustomerLat())
                .customerLng(order.getCustomerLng())
                .deliveryAddress(order.getDeliveryAddress())
                .deliveryNotes(order.getDeliveryNotes())
                .bidDeadline(order.getBidDeadline())
                .selectedBidId(order.getSelectedBidId())
                .approvedPricePerKg(order.getApprovedPricePerKg())
                .approvedDeliveryFee(order.getApprovedDeliveryFee())
                .approvedTotalAmount(order.getApprovedTotalAmount())
                .totalAmount(order.getTotalAmount())
                .deliveryFee(order.getDeliveryFee())
                .finalAmount(order.getFinalAmount())
                .bids(bidResponses)
                .createdAt(order.getCreatedAt())
                .approvedAt(order.getApprovedAt())
                .acceptedAt(order.getAcceptedAt())
                .completedAt(order.getCompletedAt())
                .cancelledAt(order.getCancelledAt())
                .cancellationReason(order.getCancellationReason())
                .build();
    }
}