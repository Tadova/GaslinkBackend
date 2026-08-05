package com.gaslink.api.modules.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStats {
    private Long totalAdmins;
    private Long superAdmins;
    private Long regularAdmins;
    private Long activeAdmins;
}
