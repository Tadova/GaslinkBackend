package com.gaslink.api.modules.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new admin")
public class CreateAdminRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "Admin email", required = true, example = "admin@gaslink.com")
    private String email;

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    @Schema(description = "Admin full name", required = true, example = "John Doe")
    private String fullName;

    @NotBlank(message = "Phone number is required")
    @Schema(description = "Phone number", required = true, example = "+2348012345678")
    private String phone;

    @Schema(description = "Admin role", example = "ADMIN", allowableValues = {"ADMIN", "SUPPORT"})
    private String role = "ADMIN";
}