package com.gaslink.api.modules.user;

import com.gaslink.api.exception.BusinessException;
import com.gaslink.api.modules.user.dto.UpdateProfileRequest;
import com.gaslink.api.modules.user.dto.UserProfileDto;
import com.gaslink.api.modules.vendor.Vendor;
import com.gaslink.api.modules.vendor.VendorRepository;
import com.gaslink.api.shared.enums.UserRole;
import com.gaslink.api.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final VendorRepository vendorRepository;

    private static final long MAX_IMAGE_SIZE_BYTES = 1_000_000; // 1MB
    private static final long MIN_IMAGE_SIZE_BYTES = 1_000; // 1KB

    public UserProfileDto getProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return toDto(user);
    }

    /**
     * Validate and process base64 avatar image
     * Works for both Customer and Vendor
     */
    private String validateAndProcessAvatar(String avatarBase64) throws BusinessException {
        if (avatarBase64 == null || avatarBase64.isEmpty()) {
            return null;
        }

        try {
            // Remove data URL prefix if present
            String base64Data = avatarBase64;
            if (avatarBase64.contains(",")) {
                base64Data = avatarBase64.substring(avatarBase64.indexOf(",") + 1);
            }

            // Check if there's actual data
            if (base64Data.trim().isEmpty()) {
                throw new BusinessException("Invalid image format: No image data found");
            }

            // Decode base64 to bytes
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);

            // Check minimum size (1KB)
            if (imageBytes.length < MIN_IMAGE_SIZE_BYTES) {
                throw new BusinessException("Image seems too small (minimum 1KB). Please upload a valid image.");
            }

            // Check maximum size (1MB)
            if (imageBytes.length > MAX_IMAGE_SIZE_BYTES) {
                throw new BusinessException("Image size must be less than 1MB. Current size: " +
                        (imageBytes.length / 1024) + "KB");
            }

            // Validate image format
            if (!isValidImageFormat(imageBytes)) {
                throw new BusinessException("Invalid image format. Please upload a valid JPEG, PNG, or GIF image.");
            }

            // Return the original base64 string
            return avatarBase64;

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

        // JPEG (FF D8 FF)
        if (imageBytes[0] == (byte) 0xFF && imageBytes[1] == (byte) 0xD8 && imageBytes[2] == (byte) 0xFF) {
            return true;
        }

        // PNG (89 50 4E 47)
        if (imageBytes[0] == (byte) 0x89 && imageBytes[1] == (byte) 0x50 &&
                imageBytes[2] == (byte) 0x4E && imageBytes[3] == (byte) 0x47) {
            return true;
        }

        // GIF (47 49 46 38)
        if (imageBytes[0] == (byte) 0x47 && imageBytes[1] == (byte) 0x49 &&
                imageBytes[2] == (byte) 0x46 && imageBytes[3] == (byte) 0x38) {
            return true;
        }

        // WebP (52 49 46 46)
        if (imageBytes[0] == (byte) 0x52 && imageBytes[1] == (byte) 0x49 &&
                imageBytes[2] == (byte) 0x46 && imageBytes[3] == (byte) 0x46) {
            return true;
        }

        return false;
    }

    /**
     * Update user profile - Works for both Customer and Vendor
     */
    @Transactional
    public UserProfileDto updateProfile(UUID userId, UpdateProfileRequest request) throws BusinessException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Update basic fields (email cannot be changed)
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }

        // Phone can be updated - check if already taken by another user
        if (request.getPhone() != null && !request.getPhone().equals(user.getPhone())) {
            if (userRepository.findByPhone(request.getPhone()).isPresent()) {
                throw new BusinessException("Phone number already registered by another user");
            }
            user.setPhone(request.getPhone());
        }

        // ✅ Process avatar image (works for BOTH Customer and Vendor)
        if (request.getAvatarBase64() != null && !request.getAvatarBase64().isEmpty()) {
            String processedAvatar = validateAndProcessAvatar(request.getAvatarBase64());
            user.setAvatarUrl(processedAvatar);
            log.info("🖼️ Avatar updated for user: {} (Role: {})", user.getEmail(), user.getRole());
        }

        if (request.getPushToken() != null) {
            user.setPushToken(request.getPushToken());
        }

        User updatedUser = userRepository.save(user);

        // If user is a vendor, update vendor-specific fields
        if (user.getRole() == UserRole.VENDOR) {
            updateVendorProfile(userId, request);
        }

        log.info("✅ Profile updated for user: {} (Role: {})", user.getEmail(), user.getRole());
        return toDto(updatedUser);
    }

    /**
     * Update vendor-specific fields
     */
    @Transactional
    public void updateVendorProfile(UUID userId, UpdateProfileRequest request) throws BusinessException {
        Vendor vendor = vendorRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

        if (request.getBusinessName() != null && !request.getBusinessName().isEmpty()) {
            vendor.setBusinessName(request.getBusinessName());
        }

        if (request.getBusinessAddress() != null && !request.getBusinessAddress().isEmpty()) {
            // Check if business address is already taken by another vendor
            if (vendorRepository.existsByBusinessAddressAndIdNot(request.getBusinessAddress(), userId)) {
                throw new BusinessException("Business address already registered by another vendor");
            }
            vendor.setBusinessAddress(request.getBusinessAddress());
        }

        if (request.getLat() != null && request.getLng() != null) {
            // Check if location is already taken by another vendor
            if (vendorRepository.existsByLocationAndIdNot(request.getLat(), request.getLng(), userId)) {
                throw new BusinessException("Location already registered by another vendor");
            }
            vendor.setLat(request.getLat());
            vendor.setLng(request.getLng());
        }

        if (request.getServiceRadiusKm() != null) {
            vendor.setServiceRadiusKm(request.getServiceRadiusKm());
        }

        vendorRepository.save(vendor);
        log.info("🏪 Vendor business details updated for user: {}", userId);
    }

    @Transactional
    public void setUserStatus(UUID userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setActive(active);
        userRepository.save(user);
        log.info("👤 User status updated: {} -> {}", user.getEmail(), active ? "ACTIVE" : "INACTIVE");
    }

    private UserProfileDto toDto(User user) {
        return new UserProfileDto(
                user.getId(),
                user.getFullName(),
                user.getPhone(),
                user.getEmail(),
                user.getRole(),
                user.getAvatarUrl()
        );
    }
}