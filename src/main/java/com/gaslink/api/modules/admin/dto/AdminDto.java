package com.gaslink.api.modules.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Admin response DTO")
public class AdminDto {

    @Schema(description = "Admin ID")
    private UUID id;

    @Schema(description = "Email")
    private String email;

    @Schema(description = "Full name")
    private String fullName;

    @Schema(description = "Phone number")
    private String phone;

    @Schema(description = "Admin role")
    private String role;

    @Schema(description = "Admin status")
    private String status;

    @Schema(description = "Is active")
    private boolean isActive;

    @Schema(description = "Created at")
    private Instant createdAt;

    @Schema(description = "Last login")
    private Instant lastLogin;
}