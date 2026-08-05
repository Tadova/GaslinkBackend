package com.gaslink.api.modules.order;

import com.gaslink.api.shared.enums.BidStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderBidRepository extends JpaRepository<OrderBid, UUID> {

    List<OrderBid> findByOrderId(UUID orderId);

    List<OrderBid> findByOrderIdAndStatus(UUID orderId, BidStatus status);

    List<OrderBid> findByOrderIdAndStatusOrderByPricePerKgAsc(UUID orderId, BidStatus status);

    Optional<OrderBid> findByOrderIdAndVendorId(UUID orderId, UUID vendorId);

    boolean existsByOrderIdAndVendorId(UUID orderId, UUID vendorId);

    long countByOrderId(UUID orderId);

    long countByOrderIdAndStatus(UUID orderId, BidStatus status);

    // REMOVE THIS METHOD - bidDeadline doesn't exist in OrderBid
    // List<OrderBid> findByStatusAndBidDeadlineBefore(BidStatus status, Instant now);

    // Instead, use this for expiring bids
    @Query("SELECT b FROM OrderBid b WHERE b.status = :status AND b.expiresAt < :now")
    List<OrderBid> findByStatusAndExpiresAtBefore(@Param("status") BidStatus status, @Param("now") Instant now);

    @Query("SELECT COUNT(b) FROM OrderBid b WHERE b.vendorId = :vendorId AND b.status = :status")
    long countByVendorIdAndStatus(@Param("vendorId") UUID vendorId, @Param("status") BidStatus status);

    @Query("SELECT b FROM OrderBid b WHERE b.orderId = :orderId AND b.status = :status ORDER BY b.pricePerKg ASC")
    List<OrderBid> findLowestBidsForOrder(@Param("orderId") UUID orderId, @Param("status") BidStatus status);
}