package com.gaslink.api.modules.vendor;

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
public class PendingVendorApplication {
    private UUID id;
    private String businessName;
    private String ownerName;
    private String email;
    private String phone;
    private String businessAddress;
    private LocalDateTime appliedAt;
}