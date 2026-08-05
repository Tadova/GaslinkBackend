package com.gaslink.api.modules.admin.service;

import com.gaslink.api.exception.BusinessException;
//import com.gaslink.api.modules.admin.dto.VendorManagementDTOs.*;
import com.gaslink.api.modules.subscription.SubscriptionService;
import com.gaslink.api.modules.vendor.dto.VendorStatisticsDto;
import com.gaslink.api.modules.email.EmailService;
import com.gaslink.api.modules.user.User;
import com.gaslink.api.modules.user.UserRepository;
import com.gaslink.api.modules.vendor.PendingVendorApplication;
import com.gaslink.api.modules.vendor.Vendor;
import com.gaslink.api.modules.vendor.VendorRepository;
import com.gaslink.api.modules.vendor.dto.VendorDetailResponse;
import com.gaslink.api.modules.vendor.dto.VendorListResponse;
import com.gaslink.api.modules.vendor.dto.VendorVerificationRequest;
import com.gaslink.api.shared.enums.UserRole;
import com.gaslink.api.shared.enums.VerificationStatus;
//import com.gaslink.api.shared.exception.BusinessException;
import com.gaslink.api.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VendorManagementService {

    private final VendorRepository vendorRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final SubscriptionService subscriptionService;

    /**
     * Get all vendors with pagination and filters
     */
    public Page<VendorListResponse> getAllVendors(Pageable pageable, String status, Boolean verified) {
        Page<Vendor> vendors;

        if (status != null) {
            VerificationStatus verificationStatus = VerificationStatus.valueOf(status.toUpperCase());
            vendors = vendorRepository.findByVerificationStatus(verificationStatus, pageable);
        } else {
            vendors = vendorRepository.findAll(pageable);
        }

        return vendors.map(this::toVendorListResponse);
    }

    /**
     * Get pending vendor applications
     */
    public List<PendingVendorApplication> getPendingVendorApplications() {
        List<Vendor> pendingVendors = vendorRepository.findByVerificationStatus(VerificationStatus.PENDING);
        return pendingVendors.stream()
                .map(this::toPendingVendorApplication)
                .collect(Collectors.toList());
    }

    /**
     * Get vendor details by ID
     */
    public VendorDetailResponse getVendorDetails(UUID vendorId) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));
        return toVendorDetailResponse(vendor);
    }

    /**
     * Verify or reject vendor
     */
    @Transactional
    public void verifyVendor(UUID vendorId, VendorVerificationRequest request) throws BusinessException {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

        // Validate status
        if (!request.getStatus().equalsIgnoreCase("VERIFIED") &&
                !request.getStatus().equalsIgnoreCase("REJECTED")) {
            throw new BusinessException("Invalid status. Must be 'VERIFIED' or 'REJECTED'");
        }

        // Check if vendor is already processed
        if (vendor.getVerificationStatus() == VerificationStatus.VERIFIED) {
            throw new BusinessException("Vendor is already verified");
        }
        if (vendor.getVerificationStatus() == VerificationStatus.REJECTED) {
            throw new BusinessException("Vendor is already rejected");
        }

        // Get the user associated with this vendor
        User user = userRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for this vendor"));

        if (request.getStatus().equalsIgnoreCase("VERIFIED")) {
            // Verify the vendor
            vendor.setVerificationStatus(VerificationStatus.VERIFIED);

            // Update user role to VENDOR
            user.setRole(UserRole.VENDOR);
            userRepository.save(user);

            log.info("✅ Vendor verified: {} ({})", vendor.getBusinessName(), vendorId);

            // Send verification email
            emailService.sendVendorVerificationEmail(
                    user.getEmail(),
                    user.getFullName(),
                    vendor.getBusinessName(),
                    "VERIFIED",
                    null
            );

            // Send welcome email to vendor
            emailService.sendVendorWelcomeEmail(
                    user.getEmail(),
                    user.getFullName(),
                    vendor.getBusinessName()
            );

            // Activate free trial subscription
            subscriptionService.activateFreeTrial(vendorId);

        } else if (request.getStatus().equalsIgnoreCase("REJECTED")) {
            // Validate rejection reason
            if (request.getRejectionReason() == null || request.getRejectionReason().trim().isEmpty()) {
                throw new BusinessException("Rejection reason is required when rejecting a vendor");
            }

            // Reject the vendor
            vendor.setVerificationStatus(VerificationStatus.REJECTED);

            log.info("❌ Vendor rejected: {} ({})", vendor.getBusinessName(), vendorId);

            // Send rejection email
            emailService.sendVendorVerificationEmail(
                    user.getEmail(),
                    user.getFullName(),
                    vendor.getBusinessName(),
                    "REJECTED",
                    request.getRejectionReason()
            );
        }

        vendorRepository.save(vendor);
    }

    /**
     * Suspend vendor
     */
    @Transactional
    public void suspendVendor(UUID vendorId, String reason) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

        vendor.setAccountStatus(com.gaslink.api.shared.enums.VendorAccountStatus.DISABLED);
        vendor.setAccountDisabledReason(reason);
        vendor.setOpen(false);

        vendorRepository.save(vendor);
        log.info("🔒 Vendor suspended: {} ({})", vendor.getBusinessName(), vendorId);
    }

    /**
     * Unsuspend vendor
     */
    @Transactional
    public void unsuspendVendor(UUID vendorId) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

        vendor.setAccountStatus(com.gaslink.api.shared.enums.VendorAccountStatus.ENABLED);
        vendor.setAccountDisabledReason(null);

        vendorRepository.save(vendor);
        log.info("🔓 Vendor unsuspended: {} ({})", vendor.getBusinessName(), vendorId);
    }

    /**
     * Delete vendor
     */
    @Transactional
    public void deleteVendor(UUID vendorId) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

        // Soft delete - just mark as rejected
        vendor.setVerificationStatus(VerificationStatus.REJECTED);
        vendor.setAccountStatus(com.gaslink.api.shared.enums.VendorAccountStatus.DISABLED);

        vendorRepository.save(vendor);
        log.info("🗑️ Vendor deleted: {} ({})", vendor.getBusinessName(), vendorId);
    }

    // ========== MAPPING METHODS ==========

    private VendorListResponse toVendorListResponse(Vendor vendor) {
        User user = userRepository.findById(vendor.getId()).orElse(null);

        return VendorListResponse.builder()
                .id(vendor.getId())
                .businessName(vendor.getBusinessName())
                .businessAddress(vendor.getBusinessAddress())
                .ownerName(user != null ? user.getFullName() : "Unknown")
                .email(user != null ? user.getEmail() : "Unknown")
                .phone(user != null ? user.getPhone() : "Unknown")
                .status(vendor.getVerificationStatus().name())
                .verified(vendor.getVerificationStatus() == VerificationStatus.VERIFIED)
                .registeredAt(vendor.getCreatedAt() != null ?
                        LocalDateTime.from(vendor.getCreatedAt()) : null)
                .verifiedAt(null) // Add if you have this field
                .rejectionReason(vendor.getAccountDisabledReason())
                .build();
    }

    private VendorDetailResponse toVendorDetailResponse(Vendor vendor) {
        User user = userRepository.findById(vendor.getId()).orElse(null);

        return VendorDetailResponse.builder()
                .id(vendor.getId())
                .businessName(vendor.getBusinessName())
                .businessAddress(vendor.getBusinessAddress())
                .ownerName(user != null ? user.getFullName() : "Unknown")
                .email(user != null ? user.getEmail() : "Unknown")
                .phone(user != null ? user.getPhone() : "Unknown")
                .status(vendor.getVerificationStatus().name())
                .verified(vendor.getVerificationStatus() == VerificationStatus.VERIFIED)
                .rejectionReason(vendor.getAccountDisabledReason())
                .registeredAt(vendor.getCreatedAt() != null ?
                        LocalDateTime.from(vendor.getCreatedAt()) : null)
                .rating(vendor.getRating())
                .totalReviews(vendor.getTotalReviews())
                .isOpen(vendor.isOpen())
                .accountStatus(vendor.getAccountStatus().name())
                .build();
    }

    private PendingVendorApplication toPendingVendorApplication(Vendor vendor) {
        User user = userRepository.findById(vendor.getId()).orElse(null);

        return PendingVendorApplication.builder()
                .id(vendor.getId())
                .businessName(vendor.getBusinessName())
                .ownerName(user != null ? user.getFullName() : "Unknown")
                .email(user != null ? user.getEmail() : "Unknown")
                .phone(user != null ? user.getPhone() : "Unknown")
                .businessAddress(vendor.getBusinessAddress())
                .appliedAt(vendor.getCreatedAt() != null ?
                        LocalDateTime.from(vendor.getCreatedAt()) : null)
                .build();
    }

    // Add to VendorManagementService.java

    public VendorStatisticsDto getVendorStatistics() {
        long totalVendors = vendorRepository.count();
        long pendingVendors = vendorRepository.countByVerificationStatus(VerificationStatus.PENDING);
        long verifiedVendors = vendorRepository.countByVerificationStatus(VerificationStatus.VERIFIED);
        long rejectedVendors = vendorRepository.countByVerificationStatus(VerificationStatus.REJECTED);

        // Get status distribution
        List<Object[]> distribution = vendorRepository.countVendorsByVerificationStatus();
        List<VendorStatisticsDto.StatusDistribution> statusDistribution = distribution.stream()
                .map(row -> VendorStatisticsDto.StatusDistribution.builder()
                        .status(row[0].toString())
                        .count((Long) row[1])
                        .percentage(((Long) row[1] * 100.0) / totalVendors)
                        .build())
                .collect(Collectors.toList());

        return VendorStatisticsDto.builder()
                .totalVendors(totalVendors)
                .pendingVendors(pendingVendors)
                .verifiedVendors(verifiedVendors)
                .rejectedVendors(rejectedVendors)
                .newVendorsThisMonth(vendorRepository.countNewVendorsThisMonth())
                .statusDistribution(statusDistribution)
                .build();
    }
}