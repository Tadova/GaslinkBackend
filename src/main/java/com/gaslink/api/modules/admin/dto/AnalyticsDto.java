package com.gaslink.api.modules.admin.dto;

import com.gaslink.api.modules.admin.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsDto {
    private UserStats userStats;
    private VendorStats vendorStats;
    private OrderStats orderStats;
    private RevenueStats revenueStats;
    private AdminStats adminStats;
}