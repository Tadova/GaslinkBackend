package com.gaslink.api.modules.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueStats {
    private Double totalRevenue;
    private Double revenueThisMonth;
    private Double platformFee;
    private Double vendorPayouts;
}