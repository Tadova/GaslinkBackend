package com.gaslink.api.modules.subscription;

import com.gaslink.api.shared.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    Optional<Subscription> findTopByVendorIdOrderByExpiresAtDesc(UUID vendorId);

    Optional<Subscription> findTopByVendorIdAndStatusOrderByExpiresAtDesc(UUID vendorId, SubscriptionStatus status);

    List<Subscription> findByStatusAndExpiresAtBefore(SubscriptionStatus status, Instant now);

    @Query("SELECT s FROM Subscription s WHERE s.vendorId = :vendorId AND s.status = :status ORDER BY s.expiresAt DESC")
    Optional<Subscription> findActiveSubscription(@Param("vendorId") UUID vendorId, @Param("status") SubscriptionStatus status);

    @Query("SELECT s FROM Subscription s WHERE s.status = :status AND s.expiresAt BETWEEN :start AND :end")
    List<Subscription> findSubscriptionsExpiringBetween(
            @Param("status") SubscriptionStatus status,
            @Param("start") Instant start,
            @Param("end") Instant end
    );

    boolean existsByVendorIdAndStatus(UUID vendorId, SubscriptionStatus status);

    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.vendorId = :vendorId AND s.status = :status")
    long countActiveSubscriptions(@Param("vendorId") UUID vendorId, @Param("status") SubscriptionStatus status);
}