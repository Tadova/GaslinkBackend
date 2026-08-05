package com.gaslink.api.modules.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update user profile")
public class UpdateProfileRequest {

    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    @Schema(description = "Full name", example = "John Doe Updated")
    private String fullName;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number format")
    @Schema(description = "Phone number (can be updated)", example = "+2348012345678")
    private String phone;

    @Schema(description = "Avatar image in base64 format (max 1MB)",
            example = "data:image/jpeg;base64,/9j/4AAQSkZJRg...")
    private String avatarBase64;

    @Schema(description = "Push notification token")
    private String pushToken;

    // ===== VENDOR SPECIFIC FIELDS =====
    @Schema(description = "Business name (vendor only)", example = "Super Gas Supply")
    private String businessName;

    @Schema(description = "Business address (vendor only)", example = "123 Lagos Street, Lagos")
    private String businessAddress;

    @Schema(description = "Business latitude (vendor only)", example = "6.5244")
    private Double lat;

    @Schema(description = "Business longitude (vendor only)", example = "3.3792")
    private Double lng;

    @Schema(description = "Service radius in km (vendor only)", example = "10.0")
    private Double serviceRadiusKm;
}