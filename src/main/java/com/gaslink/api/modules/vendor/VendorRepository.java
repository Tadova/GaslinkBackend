package com.gaslink.api.modules.vendor;

import com.gaslink.api.shared.enums.VendorAccountStatus;
import com.gaslink.api.shared.enums.VerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VendorRepository extends JpaRepository<Vendor, UUID> {

    // ================================================================
    // EXISTENCE CHECKS (For Registration Validation)
    // ================================================================

    /**
     * Check if NIN is already registered
     */
    boolean existsByNin(String nin);

    /**
     * Check if business address is already registered
     */
    boolean existsByBusinessAddress(String businessAddress);

    /**
     * Check if location (lat/lng) is already registered
     */
    @Query("SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END FROM Vendor v WHERE v.lat = :lat AND v.lng = :lng")
    boolean existsByLocation(@Param("lat") Double lat, @Param("lng") Double lng);

    /**
     * Check if NIN exists excluding a specific vendor (for updates)
     */
    @Query("SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END FROM Vendor v WHERE v.nin = :nin AND v.id != :excludeVendorId")
    boolean existsByNinAndIdNot(@Param("nin") String nin, @Param("excludeVendorId") UUID excludeVendorId);

    /**
     * Check if location exists excluding a specific vendor (for updates)
     */
    @Query("SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END FROM Vendor v WHERE v.lat = :lat AND v.lng = :lng AND v.id != :excludeVendorId")
    boolean existsByLocationAndIdNot(@Param("lat") Double lat, @Param("lng") Double lng, @Param("excludeVendorId") UUID excludeVendorId);

    /**
     * Check if business address exists excluding a specific vendor (for updates)
     */
    @Query("SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END FROM Vendor v WHERE v.businessAddress = :address AND v.id != :excludeVendorId")
    boolean existsByBusinessAddressAndIdNot(@Param("address") String address, @Param("excludeVendorId") UUID excludeVendorId);

    // ================================================================
    // BASIC QUERIES
    // ================================================================

    /**
     * Find vendor by user ID (vendor ID is the same as user ID)
     */
    Optional<Vendor> findById(UUID id);

    /**
     * Find vendor by user ID (alias)
     */
    default Optional<Vendor> findByUserId(UUID userId) {
        return findById(userId);
    }

    /**
     * Find vendors by verification status
     */
    List<Vendor> findByVerificationStatus(VerificationStatus status);

    /**
     * Find vendors by verification status with pagination
     */
    Page<Vendor> findByVerificationStatus(VerificationStatus status, Pageable pageable);

    /**
     * Find vendors by verification status and subscription status
     * Used for notifying nearby vendors about new orders
     */
    List<Vendor> findByVerificationStatusAndSubscriptionStatus(VerificationStatus verificationStatus, String subscriptionStatus);

    // ================================================================
    // NEARBY VENDORS
    // ================================================================

    /**
     * Find nearby verified vendors with active accounts
     */
    @Query(value = """
        SELECT 
            v.id, 
            v.business_name, 
            v.business_address, 
            v.nin, 
            v.lat, 
            v.lng, 
            v.service_radius_km, 
            v.verification_status, 
            v.account_status, 
            v.account_disabled_reason, 
            v.rating, 
            v.total_reviews,
            v.is_open, 
            v.created_at, 
            v.updated_at,
            (6371 * acos(cos(radians(:lat)) * cos(radians(v.lat)) * 
            cos(radians(v.lng) - radians(:lng)) + sin(radians(:lat)) * sin(radians(v.lat)))) AS distance_km,
            (SELECT MIN(i.price) FROM inventory i WHERE i.vendor_id = v.id AND i.quantity > 0) AS lowest_price
        FROM vendors v
        WHERE v.verification_status = 'VERIFIED' 
        AND v.account_status = 'ENABLED'
        AND v.subscription_status = 'ACTIVE'
        ORDER BY distance_km
        LIMIT :limit
    """, nativeQuery = true)
    List<Object[]> findNearbyVendors(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("limit") int limit
    );

    // ================================================================
    // STATUS COUNTS (For Analytics)
    // ================================================================

    /**
     * Count vendors by verification status
     */
    @Query("SELECT COUNT(v) FROM Vendor v WHERE v.verificationStatus = :status")
    long countByVerificationStatus(@Param("status") VerificationStatus status);

    /**
     * Get all pending vendors
     */
    @Query("SELECT v FROM Vendor v WHERE v.verificationStatus = 'PENDING' ORDER BY v.createdAt DESC")
    List<Vendor> findAllPendingVendors();

    /**
     * Get vendor count grouped by verification status
     */
    @Query("SELECT v.verificationStatus, COUNT(v) FROM Vendor v GROUP BY v.verificationStatus")
    List<Object[]> countVendorsByVerificationStatus();

    // ================================================================
    // ACCOUNT STATUS
    // ================================================================

    /**
     * Find vendors by account status
     */
    List<Vendor> findByAccountStatus(VendorAccountStatus accountStatus);

    /**
     * Find active vendors (ENABLED account status)
     */
    default List<Vendor> findActiveVendors() {
        return findByAccountStatus(VendorAccountStatus.ENABLED);
    }

    // ================================================================
    // SUBSCRIPTION STATUS
    // ================================================================

    /**
     * Find vendors by subscription status
     */
    List<Vendor> findBySubscriptionStatus(String subscriptionStatus);

    /**
     * Find vendors with active subscription
     */
    default List<Vendor> findActiveSubscriptionVendors() {
        return findBySubscriptionStatus("ACTIVE");
    }

    // ================================================================
    // NEW VENDORS (For Analytics)
    // ================================================================

    /**
     * Count new vendors since a specific date
     */
    @Query("SELECT COUNT(v) FROM Vendor v WHERE v.createdAt >= :startDate")
    long countNewVendorsSince(@Param("startDate") Instant startDate);

    /**
     * Count new vendors this month
     */
    @Query("SELECT COUNT(v) FROM Vendor v WHERE v.createdAt >= :startOfMonth")
    long countNewVendorsThisMonth(@Param("startOfMonth") Instant startOfMonth);

    /**
     * Default method to count new vendors this month
     * Uses LocalDate to calculate the start of the month
     */
    default long countNewVendorsThisMonth() {
        LocalDate now = LocalDate.now();
        LocalDate firstDayOfMonth = now.withDayOfMonth(1);
        Instant startOfMonth = firstDayOfMonth.atStartOfDay(ZoneId.systemDefault()).toInstant();
        return countNewVendorsThisMonth(startOfMonth);
    }

    /**
     * Count new vendors this week
     */
    default long countNewVendorsThisWeek() {
        LocalDate now = LocalDate.now();
        LocalDate startOfWeek = now.with(java.time.DayOfWeek.MONDAY);
        Instant startOfWeekInstant = startOfWeek.atStartOfDay(ZoneId.systemDefault()).toInstant();
        return countNewVendorsSince(startOfWeekInstant);
    }

    /**
     * Count new vendors today
     */
    default long countNewVendorsToday() {
        LocalDate now = LocalDate.now();
        Instant startOfDay = now.atStartOfDay(ZoneId.systemDefault()).toInstant();
        return countNewVendorsSince(startOfDay);
    }
}