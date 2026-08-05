package com.gaslink.api.modules.vendor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorStatisticsDto {
    private Long totalVendors;
    private Long pendingVendors;
    private Long verifiedVendors;
    private Long rejectedVendors;
    private Long suspendedVendors;
    private Long newVendorsThisMonth;
    private List<StatusDistribution> statusDistribution;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusDistribution {
        private String status;
        private Long count;
        private Double percentage;
    }
}