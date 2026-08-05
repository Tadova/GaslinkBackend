package com.gaslink.api.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
@Schema(description = "Registration request")
public class RegisterRequest {

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    @Schema(description = "Full name", required = true, example = "John Doe")
    private String fullName;

    @NotBlank(message = "Phone number is required")
    @Schema(description = "Phone number", required = true, example = "+2348012345678")
    private String phone;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "Email", required = true, example = "john@example.com")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Schema(description = "Password", required = true, example = "SecurePass123!")
    private String password;

    @NotBlank(message = "Role is required")
    @Schema(description = "User role", required = true, allowableValues = {"CUSTOMER", "VENDOR"}, example = "CUSTOMER")
    private String role;

    // ===== VENDOR SPECIFIC FIELDS =====
    @Schema(description = "Business name (required for VENDOR)", example = "Super Gas Supply")
    private String businessName;

    @Schema(description = "Business address (required for VENDOR)", example = "123 Lagos Street, Lagos")
    private String businessAddress;

    @Schema(description = "NIN (required for VENDOR) - Must be exactly 11 digits", example = "12345678901")
    @Pattern(regexp = "^\\d{11}$", message = "NIN must be exactly 11 digits")
    private String nin;

    @Schema(description = "Business latitude (required for VENDOR)", example = "6.5244")
    private Double lat;

    @Schema(description = "Business longitude (required for VENDOR)", example = "3.3792")
    private Double lng;

    @Schema(description = "Service radius in km (required for VENDOR)", example = "10.0")
    private Double serviceRadiusKm;

    @Schema(description = "Push notification token (device token)")
    private String pushToken;
}