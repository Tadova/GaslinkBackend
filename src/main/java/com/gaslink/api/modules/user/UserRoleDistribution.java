package com.gaslink.api.modules.user;

import io.lettuce.core.api.async.BaseRedisAsyncCommands;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleDistribution {
    private String role;
    private Long count;
    private Double percentage;
}
