package com.gaslink.api.modules.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorStats {
    private Long totalVendors;
    private Long verifiedVendors;
    private Long pendingVendors;
    private Long rejectedVendors;
}
