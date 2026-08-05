package com.gaslink.api.modules.vendor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorDetailResponse {
    private UUID id;
    private String businessName;
    private String businessAddress;
    private String ownerName;
    private String email;
    private String phone;
    private String status;
    private Boolean verified;
    private String rejectionReason;
    private LocalDateTime registeredAt;
    private BigDecimal rating;
    private Integer totalReviews;
    private Boolean isOpen;
    private String accountStatus;
}