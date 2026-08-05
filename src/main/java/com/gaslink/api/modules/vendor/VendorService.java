package com.gaslink.api.modules.vendor;
import com.gaslink.api.modules.email.EmailService;
import com.gaslink.api.modules.vendor.dto.*;
import com.gaslink.api.modules.user.UserRepository;
import com.gaslink.api.modules.user.User;
import com.gaslink.api.shared.enums.*;
import com.gaslink.api.shared.exception.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j

public class VendorService {
    private final VendorRepository vendorRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;


    public VendorService(VendorRepository vendorRepository, UserRepository userRepository, EmailService emailService) {
        this.vendorRepository = vendorRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @Transactional
    public VendorProfileDto register(UUID userId, VendorRegistrationRequest req) {
        if (vendorRepository.existsById(userId))
            throw new BusinessRuleException("Vendor profile already exists");
        if (vendorRepository.existsByNin(req.getNin()))
            throw new BusinessRuleException("NIN already registered with another vendor");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setRole(UserRole.VENDOR);
        userRepository.save(user);

        Vendor vendor = Vendor.builder()
                .id(userId)
                .businessName(req.getBusinessName())
                .businessAddress(req.getBusinessAddress())
                .nin(req.getNin())
                .lat(req.getLat())
                .lng(req.getLng())
                .serviceRadiusKm(req.getServiceRadiusKm())
                .verificationStatus(VerificationStatus.PENDING)
                .build();

        Vendor savedVendor = vendorRepository.save(vendor);

        // Send notification to all admins
        notifyAdminsAboutNewVendor(user, savedVendor);

        return toDto(savedVendor);
    }

    public VendorProfileDto getProfile(UUID vendorId) {
        return toDto(vendorRepository.findById(vendorId).orElseThrow(() -> new ResourceNotFoundException("Vendor not found")));
    }

    @Transactional
    public VendorProfileDto updateVerificationStatus(UUID vendorId, UpdateVendorStatusRequest req) {
        Vendor v = vendorRepository.findById(vendorId).orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));
        v.setVerificationStatus(req.getVerificationStatus());
        return toDto(vendorRepository.save(v));
    }

    @Transactional
    public VendorProfileDto disableVendor(UUID vendorId, String reason) {
        Vendor v = vendorRepository.findById(vendorId).orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));
        v.setAccountStatus(VendorAccountStatus.DISABLED);
        v.setAccountDisabledReason(reason);
        v.setOpen(false);
        return toDto(vendorRepository.save(v));
    }

    @Transactional
    public VendorProfileDto enableVendor(UUID vendorId) {
        Vendor v = vendorRepository.findById(vendorId).orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));
        v.setAccountStatus(VendorAccountStatus.ENABLED);
        v.setAccountDisabledReason(null);
        return toDto(vendorRepository.save(v));
    }

    public List<NearbyVendorDto> getNearbyVendors(double lat, double lng, int limit) {
        List<Object[]> results = vendorRepository.findNearbyVendors(lat, lng, limit);

        return results.stream()
                .map(row -> {
                    Object[] r = (Object[]) row;

                    UUID id = r[0] != null ? UUID.fromString(r[0].toString()) : null;
                    String businessName = r[1] != null ? r[1].toString() : "";
                    Double vendorLat = r[4] != null ? Double.parseDouble(r[4].toString()) : null;
                    Double vendorLng = r[5] != null ? Double.parseDouble(r[5].toString()) : null;
                    BigDecimal rating = r[10] != null ? new BigDecimal(r[10].toString()) : BigDecimal.ZERO;
                    int totalReviews = r[11] != null ? Integer.parseInt(r[11].toString()) : 0;
                    boolean isOpen = r[12] != null ? Boolean.parseBoolean(r[12].toString()) : false;
                    double distanceKm = r[15] != null ? Double.parseDouble(r[15].toString()) : 0;

                    // Get lowest price from the query result (index 16)
                    BigDecimal lowestPrice = r[16] != null ? new BigDecimal(r[16].toString()) : null;

                    return NearbyVendorDto.builder()
                            .id(id)
                            .businessName(businessName)
                            .lat(vendorLat)
                            .lng(vendorLng)
                            .rating(rating)
                            .totalReviews(totalReviews)
                            .distanceKm(distanceKm)
                            .isOpen(isOpen)
                            .lowestPrice(lowestPrice)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public VendorProfileDto toggleOpen(UUID vendorId, boolean open) {
        Vendor v = vendorRepository.findById(vendorId).orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));
        if (v.getAccountStatus() == VendorAccountStatus.DISABLED)
            throw new ForbiddenException("Your account is disabled.");
        v.setOpen(open);
        return toDto(vendorRepository.save(v));
    }

    private VendorProfileDto toDto(Vendor v) {
        return VendorProfileDto.builder().id(v.getId()).businessName(v.getBusinessName())
                .businessAddress(v.getBusinessAddress()).nin(v.getNin())
                .lat(v.getLat()).lng(v.getLng()).serviceRadiusKm(v.getServiceRadiusKm())
                .verificationStatus(v.getVerificationStatus()).accountStatus(v.getAccountStatus())
                .accountDisabledReason(v.getAccountDisabledReason())
                .isOpen(v.isOpen()).rating(v.getRating()).totalReviews(v.getTotalReviews()).build();
    }

    private void notifyAdminsAboutNewVendor(User vendor, Vendor vendorProfile) {
        try {
            // Get all admin users
            List<User> admins = userRepository.findByRole(UserRole.ADMIN);
            List<User> superAdmins = userRepository.findByRole(UserRole.SUPER_ADMIN);

            List<User> allAdmins = new ArrayList<>();
            allAdmins.addAll(admins);
            allAdmins.addAll(superAdmins);

            // Send email to each admin
            for (User admin : allAdmins) {
                emailService.sendNewVendorNotificationToAdmins(
                        admin.getEmail(),
                        vendor.getFullName(),
                        vendorProfile.getBusinessName(),
                        vendor.getEmail()
                );
            }

            log.info("📧 New vendor notification sent to {} admins", allAdmins.size());

        } catch (Exception e) {
            log.error("❌ Failed to send new vendor notifications: {}", e.getMessage());
        }
    }


}