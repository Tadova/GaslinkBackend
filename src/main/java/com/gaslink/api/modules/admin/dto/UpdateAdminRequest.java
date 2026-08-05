package com.gaslink.api.modules.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update an admin")
public class UpdateAdminRequest {

    @Schema(description = "Admin full name", example = "John Smith")
    private String fullName;

    @Schema(description = "Phone number", example = "+2348012345678")
    private String phone;

    @Schema(description = "Admin role", example = "SUPPORT", allowableValues = {"ADMIN", "SUPPORT"})
    private String role;

    @Schema(description = "Admin status", example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE", "SUSPENDED"})
    private String status;
}