package com.gaslink.api.modules.vendor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorListResponse {
    private UUID id;
    private String businessName;
    private String businessAddress;
    private String ownerName;
    private String email;
    private String phone;
    private String status;
    private Boolean verified;
    private LocalDateTime registeredAt;
    private LocalDateTime verifiedAt;
    private String rejectionReason;
}