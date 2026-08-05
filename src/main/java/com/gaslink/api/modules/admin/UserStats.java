package com.gaslink.api.modules.admin;

import com.gaslink.api.modules.user.UserRoleDistribution;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStats {
    private Long totalUsers;
    private Long activeUsers;
    private Long inactiveUsers;
    private Long newUsersThisMonth;
    private List<UserRoleDistribution> roleDistribution;
}