package com.gaslink.api.modules.admin;

import com.gaslink.api.modules.admin.dto.DashboardStatsDto;
import com.gaslink.api.modules.admin.dto.*;
import com.gaslink.api.modules.order.OrderRepository;
import com.gaslink.api.modules.user.UserRepository;
import com.gaslink.api.modules.user.UserService;
import com.gaslink.api.modules.vendor.VendorRepository;
import com.gaslink.api.shared.enums.*;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class AdminService {
    private final UserRepository userRepository;
    private final VendorRepository vendorRepository;
    private final OrderRepository orderRepository;
    private final UserService userService;

    public AdminService(UserRepository userRepository, VendorRepository vendorRepository,
                        OrderRepository orderRepository, UserService userService) {
        this.userRepository = userRepository;
        this.vendorRepository = vendorRepository;
        this.orderRepository = orderRepository;
        this.userService = userService;
    }

    public DashboardStatsDto getDashboardStats() {
        long totalUsers = userRepository.count();
        long activeVendors = vendorRepository.findByAccountStatus(VendorAccountStatus.ENABLED).size();
        Instant startOfDay = Instant.now().truncatedTo(ChronoUnit.DAYS);

        long ordersToday = orderRepository.findAll().stream()
                .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().isAfter(startOfDay)).count();

        // FIX: Use getFinalAmount() or getTotalAmount() instead of getTotal()
        BigDecimal revenueToday = orderRepository.findAll().stream()
                .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().isAfter(startOfDay)
                        && o.getPaymentStatus() == PaymentStatus.PAID)
                .map(o -> o.getFinalAmount() != null ? o.getFinalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long pendingVerifications = vendorRepository.findByVerificationStatus(VerificationStatus.PENDING).size();

        return new DashboardStatsDto(
                totalUsers,
                activeVendors,
                ordersToday,
                revenueToday,
                pendingVerifications,
                0
        );
    }

    public void performUserAction(AdminUserActionRequest req) {
        boolean active = "ACTIVATE".equalsIgnoreCase(req.getAction());
        userService.setUserStatus(req.getUserId(), active);
    }
}