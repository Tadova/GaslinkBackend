package com.gaslink.api.modules.order;

import com.gaslink.api.shared.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    // Customer orders
    List<Order> findByCustomerId(UUID customerId);
    Page<Order> findByCustomerId(UUID customerId, Pageable pageable);

    // Vendor orders
    List<Order> findByVendorId(UUID vendorId);
    Page<Order> findByVendorId(UUID vendorId, Pageable pageable);

    // Orders by status
    List<Order> findByStatus(OrderStatus status);
    List<Order> findByVendorIdAndStatus(UUID vendorId, OrderStatus status);
    List<Order> findByCustomerIdAndStatus(UUID customerId, OrderStatus status);

    // ===== ADD THIS METHOD =====
    List<Order> findByStatusAndCreatedAtBefore(OrderStatus status, Instant createdAt);

    // Find by reference
    Optional<Order> findByOrderReference(String orderReference);

    // Find by status and bid deadline
    List<Order> findByStatusAndBidDeadlineBefore(OrderStatus status, Instant now);

    // Count orders by status for vendor
    @Query("SELECT COUNT(o) FROM Order o WHERE o.vendorId = :vendorId AND o.status = :status")
    long countByVendorIdAndStatus(@Param("vendorId") UUID vendorId, @Param("status") OrderStatus status);

    // Count orders by status for customer
    @Query("SELECT COUNT(o) FROM Order o WHERE o.customerId = :customerId AND o.status = :status")
    long countByCustomerIdAndStatus(@Param("customerId") UUID customerId, @Param("status") OrderStatus status);

    // Get vendor order statistics
    @Query("SELECT COUNT(o), SUM(o.finalAmount) FROM Order o WHERE o.vendorId = :vendorId AND o.status = 'COMPLETED'")
    List<Object[]> getVendorOrderStats(@Param("vendorId") UUID vendorId);

    // Get customer order statistics
    @Query("SELECT COUNT(o), SUM(o.finalAmount) FROM Order o WHERE o.customerId = :customerId AND o.status = 'COMPLETED'")
    List<Object[]> getCustomerOrderStats(@Param("customerId") UUID customerId);

    // Get orders by date range
    @Query("SELECT o FROM Order o WHERE o.vendorId = :vendorId AND o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findOrdersByDateRange(
            @Param("vendorId") UUID vendorId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );

    Long countByStatus(OrderStatus orderStatus);
}