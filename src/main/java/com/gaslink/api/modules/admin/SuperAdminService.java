package com.gaslink.api.modules.admin;

import com.gaslink.api.exception.BusinessException;
import com.gaslink.api.modules.admin.dto.*;
import com.gaslink.api.modules.auth.entity.PasswordResetToken;
import com.gaslink.api.modules.auth.repository.PasswordResetTokenRepository;
import com.gaslink.api.modules.email.EmailService;
import com.gaslink.api.modules.user.User;
import com.gaslink.api.modules.user.UserRepository;
import com.gaslink.api.modules.user.UserRoleDistribution;
import com.gaslink.api.modules.user.dto.UserDetailDto;
import com.gaslink.api.modules.user.dto.UserInfoDto;
import com.gaslink.api.modules.vendor.Vendor;
import com.gaslink.api.modules.vendor.VendorRepository;
import com.gaslink.api.modules.vendor.VendorService;
import com.gaslink.api.modules.order.Order;
import com.gaslink.api.modules.order.OrderRepository;
import com.gaslink.api.modules.payment.Payment;
import com.gaslink.api.modules.payment.PaymentRepository;
import com.gaslink.api.modules.vendor.dto.VendorApplicationDto;
import com.gaslink.api.modules.vendor.dto.VendorDetailDto;
import com.gaslink.api.modules.vendor.dto.VendorInfoDto;
import com.gaslink.api.modules.vendor.dto.VendorVerificationRequest;
import com.gaslink.api.shared.enums.UserRole;
import com.gaslink.api.shared.enums.VerificationStatus;
//import com.gaslink.api.shared.exception.BusinessException;
import com.gaslink.api.shared.exception.ResourceNotFoundException;
import com.gaslink.api.util.ActivityLogDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SuperAdminService {

    private final UserRepository userRepository;
    private final VendorRepository vendorRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final VendorService vendorService;
    private final PasswordResetTokenRepository resetTokenRepository;

    private static final int RESET_TOKEN_EXPIRY_MINUTES = 30;

    // ========== DASHBOARD & ANALYTICS ==========

    public AnalyticsDto getPlatformAnalytics() {
        return AnalyticsDto.builder()
                .userStats(getUserAnalytics())
                .vendorStats(getVendorAnalytics())
                .orderStats(getOrderAnalytics())
                .revenueStats(getRevenueAnalytics())
                .adminStats(getAdminStats())
                .build();
    }

    public UserStats getUserAnalytics() {
        List<User> allUsers = userRepository.findAll();
        long totalUsers = allUsers.size();
        long activeUsers = allUsers.stream().filter(User::isActive).count();

        // Role distribution
        List<UserRoleDistribution> roleDistribution = List.of(
                UserRoleDistribution.builder()
                        .role("CUSTOMER")
                        .count(allUsers.stream().filter(u -> u.getRole() == UserRole.CUSTOMER).count())
                        .percentage((allUsers.stream().filter(u -> u.getRole() == UserRole.CUSTOMER).count() * 100.0) / totalUsers)
                        .build(),
                UserRoleDistribution.builder()
                        .role("VENDOR")
                        .count(allUsers.stream().filter(u -> u.getRole() == UserRole.VENDOR).count())
                        .percentage((allUsers.stream().filter(u -> u.getRole() == UserRole.VENDOR).count() * 100.0) / totalUsers)
                        .build(),
                UserRoleDistribution.builder()
                        .role("ADMIN")
                        .count(allUsers.stream().filter(u -> u.getRole() == UserRole.ADMIN).count())
                        .percentage((allUsers.stream().filter(u -> u.getRole() == UserRole.ADMIN).count() * 100.0) / totalUsers)
                        .build(),
                UserRoleDistribution.builder()
                        .role("SUPER_ADMIN")
                        .count(allUsers.stream().filter(u -> u.getRole() == UserRole.SUPER_ADMIN).count())
                        .percentage((allUsers.stream().filter(u -> u.getRole() == UserRole.SUPER_ADMIN).count() * 100.0) / totalUsers)
                        .build()
        );

        // Use Instant instead of LocalDateTime
        Instant thirtyDaysAgo = Instant.now().minus(30, ChronoUnit.DAYS);

        return UserStats.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .inactiveUsers(totalUsers - activeUsers)
                .newUsersThisMonth(allUsers.stream()
                        .filter(u -> u.getCreatedAt() != null &&
                                u.getCreatedAt().isAfter(thirtyDaysAgo))
                        .count())
                .roleDistribution(roleDistribution)
                .build();
    }

    public VendorStats getVendorAnalytics() {
        List<Vendor> allVendors = vendorRepository.findAll();
        long totalVendors = allVendors.size();

        return VendorStats.builder()
                .totalVendors(totalVendors)
                .verifiedVendors(allVendors.stream()
                        .filter(v -> v.getVerificationStatus() == VerificationStatus.VERIFIED)
                        .count())
                .pendingVendors(allVendors.stream()
                        .filter(v -> v.getVerificationStatus() == VerificationStatus.PENDING)
                        .count())
                .rejectedVendors(allVendors.stream()
                        .filter(v -> v.getVerificationStatus() == VerificationStatus.REJECTED)
                        .count())
                .build();
    }

    public OrderStats getOrderAnalytics() {
        return OrderStats.builder()
                .totalOrders(orderRepository.count())
                .pendingOrders(orderRepository.countByStatus(com.gaslink.api.shared.enums.OrderStatus.PENDING))
                .processingOrders(orderRepository.countByStatus(com.gaslink.api.shared.enums.OrderStatus.PROCESSING))
                .completedOrders(orderRepository.countByStatus(com.gaslink.api.shared.enums.OrderStatus.COMPLETED))
                .cancelledOrders(orderRepository.countByStatus(com.gaslink.api.shared.enums.OrderStatus.CANCELLED))
                .averageOrderValue(calculateAverageOrderValue())
                .build();
    }

    private Double calculateAverageOrderValue() {
        List<Order> completedOrders = orderRepository.findByStatus(com.gaslink.api.shared.enums.OrderStatus.COMPLETED);
        if (completedOrders.isEmpty()) return 0.0;
        return completedOrders.stream()
                .mapToDouble(o -> o.getFinalAmount() != null ? o.getFinalAmount().doubleValue() : 0.0)
                .average()
                .orElse(0.0);
    }

    public RevenueStats getRevenueAnalytics() {
        List<Payment> successfulPayments = paymentRepository.findByStatus(com.gaslink.api.shared.enums.PaymentStatus.PAID);
        double totalRevenue = successfulPayments.stream()
                .mapToDouble(p -> p.getAmount() != null ? p.getAmount().doubleValue() : 0.0)
                .sum();

        Instant monthStart = Instant.now().minus(30, ChronoUnit.DAYS);
        double revenueThisMonth = successfulPayments.stream()
                .filter(p -> p.getCreatedAt() != null && p.getCreatedAt().isAfter(monthStart))
                .mapToDouble(p -> p.getAmount() != null ? p.getAmount().doubleValue() : 0.0)
                .sum();

        double platformFee = totalRevenue * 0.10; // 10% platform fee
        double vendorPayouts = totalRevenue - platformFee;

        return RevenueStats.builder()
                .totalRevenue(totalRevenue)
                .revenueThisMonth(revenueThisMonth)
                .platformFee(platformFee)
                .vendorPayouts(vendorPayouts)
                .build();
    }

    private AdminStats getAdminStats() {
        List<User> allUsers = userRepository.findAll();
        return AdminStats.builder()
                .totalAdmins(allUsers.stream()
                        .filter(u -> u.getRole() == UserRole.ADMIN || u.getRole() == UserRole.SUPER_ADMIN)
                        .count())
                .superAdmins(allUsers.stream()
                        .filter(u -> u.getRole() == UserRole.SUPER_ADMIN)
                        .count())
                .regularAdmins(allUsers.stream()
                        .filter(u -> u.getRole() == UserRole.ADMIN)
                        .count())
                .activeAdmins(allUsers.stream()
                        .filter(u -> (u.getRole() == UserRole.ADMIN || u.getRole() == UserRole.SUPER_ADMIN) && u.isActive())
                        .count())
                .build();
    }

    // ========== ADMIN MANAGEMENT ==========

    public Page<AdminDto> getAllAdmins(Pageable pageable, String role, String status) {
        // Implementation
        return Page.empty();
    }

    public AdminDto getAdminById(UUID id) throws BusinessException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        if (user.getRole() != UserRole.ADMIN && user.getRole() != UserRole.SUPER_ADMIN) {
            throw new BusinessException("User is not an admin");
        }

        return toAdminDto(user);
    }

    @Transactional
    public AdminDto createAdmin(CreateAdminRequest request) throws BusinessException {
        // Check if user already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BusinessException("User with email " + request.getEmail() + " already exists");
        }

        if (userRepository.findByPhone(request.getPhone()).isPresent()) {
            throw new BusinessException("User with phone " + request.getPhone() + " already exists");
        }

        // Validate role
        UserRole role;
        try {
            role = UserRole.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid role. Must be ADMIN or SUPPORT");
        }

        if (role == UserRole.SUPER_ADMIN) {
            throw new BusinessException("Cannot create SUPER_ADMIN. Only existing SUPER_ADMIN can be assigned.");
        }

        // Generate random temporary password
        String tempPassword = generateRandomPassword();

        // Create new admin user
        User admin = User.builder()
                .email(request.getEmail())
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(tempPassword))
                .role(role)
                .isActive(false) // Inactive until they set their password
                .build();

        User savedAdmin = userRepository.save(admin);

        // Generate password reset token for admin to set their password
        String resetToken = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(RESET_TOKEN_EXPIRY_MINUTES);

        PasswordResetToken token = PasswordResetToken.builder()
                .token(resetToken)
                .email(savedAdmin.getEmail())
                .expiryDate(expiryDate)
                .used(false)
                .createdAt(LocalDateTime.now())
                .build();

        resetTokenRepository.save(token);

        // Send welcome email with password setup link
        try {
            emailService.sendAdminWelcomeEmail(
                    savedAdmin.getEmail(),
                    savedAdmin.getFullName(),
                    resetToken,
                    tempPassword
            );
            log.info("✅ Welcome email sent to new admin: {}", savedAdmin.getEmail());
        } catch (Exception e) {
            log.error("❌ Failed to send welcome email to admin: {}", savedAdmin.getEmail(), e);
        }

        log.info("✅ New admin created: {} with role: {}", savedAdmin.getEmail(), role);

        return toAdminDto(savedAdmin);
    }

    @Transactional
    public AdminDto updateAdmin(UUID id, UpdateAdminRequest request) throws BusinessException {
        User admin = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        if (admin.getRole() != UserRole.ADMIN && admin.getRole() != UserRole.SUPER_ADMIN) {
            throw new BusinessException("User is not an admin");
        }

        // Prevent updating SUPER_ADMIN
        if (admin.getRole() == UserRole.SUPER_ADMIN) {
            throw new BusinessException("Cannot update SUPER_ADMIN through this endpoint");
        }

        if (request.getFullName() != null && !request.getFullName().isEmpty()) {
            admin.setFullName(request.getFullName());
        }

        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            admin.setPhone(request.getPhone());
        }

        if (request.getRole() != null && !request.getRole().isEmpty()) {
            try {
                UserRole newRole = UserRole.valueOf(request.getRole().toUpperCase());
                if (newRole == UserRole.SUPER_ADMIN) {
                    throw new BusinessException("Cannot assign SUPER_ADMIN role");
                }
                admin.setRole(newRole);
            } catch (IllegalArgumentException e) {
                throw new BusinessException("Invalid role. Must be ADMIN or SUPPORT");
            }
        }

        if (request.getStatus() != null) {
            admin.setActive("ACTIVE".equalsIgnoreCase(request.getStatus()));
        }

        User updatedAdmin = userRepository.save(admin);
        log.info("✅ Admin updated: {}", updatedAdmin.getEmail());

        return toAdminDto(updatedAdmin);
    }

    @Transactional
    public void deleteAdmin(UUID id) throws BusinessException {
        User admin = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        if (admin.getRole() != UserRole.ADMIN && admin.getRole() != UserRole.SUPER_ADMIN) {
            throw new BusinessException("User is not an admin");
        }

        // Prevent deleting SUPER_ADMIN
        if (admin.getRole() == UserRole.SUPER_ADMIN) {
            throw new BusinessException("Cannot delete SUPER_ADMIN");
        }

        // Check if this is the last admin (excluding SUPER_ADMIN)
        long adminCount = userRepository.countByRole(UserRole.ADMIN);
        if (adminCount <= 1) {
            throw new BusinessException("Cannot delete the last admin. There must be at least one admin.");
        }

        // Soft delete - deactivate and set role to CUSTOMER
        admin.setActive(false);
        admin.setRole(UserRole.CUSTOMER);
        userRepository.save(admin);

        log.info("🗑️ Admin deleted (deactivated): {}", admin.getEmail());
    }

    @Transactional
    public void suspendAdmin(UUID id) throws BusinessException {
        User admin = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        if (admin.getRole() != UserRole.ADMIN && admin.getRole() != UserRole.SUPER_ADMIN) {
            throw new BusinessException("User is not an admin");
        }

        // Prevent suspending SUPER_ADMIN
        if (admin.getRole() == UserRole.SUPER_ADMIN) {
            throw new BusinessException("Cannot suspend SUPER_ADMIN");
        }

        admin.setActive(false);
        userRepository.save(admin);

        log.info("🔒 Admin suspended: {}", admin.getEmail());
    }

    @Transactional
    public void activateAdmin(UUID id) throws BusinessException {
        User admin = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        if (admin.getRole() != UserRole.ADMIN && admin.getRole() != UserRole.SUPER_ADMIN) {
            throw new BusinessException("User is not an admin");
        }

        admin.setActive(true);
        userRepository.save(admin);

        log.info("🔓 Admin activated: {}", admin.getEmail());
    }

    // ========== VENDOR MANAGEMENT ==========

    public Page<VendorInfoDto> getAllVendors(Pageable pageable, String status, Boolean verified) {
        // Implementation
        return Page.empty();
    }

    public VendorDetailDto getVendorDetails(UUID id) {
        // Implementation
        return null;
    }

    public List<VendorApplicationDto> getPendingVendorApplications() {
        // Implementation
        return List.of();
    }

    public void verifyVendor(UUID vendorId, VendorVerificationRequest request) {
        // Implementation
    }

    public void suspendVendor(UUID vendorId, String reason) {
        // Implementation
    }

    public void unsuspendVendor(UUID vendorId) {
        // Implementation
    }

    public void deleteVendor(UUID vendorId) {
        // Implementation
    }

    // ========== USER MANAGEMENT ==========

    public Page<UserInfoDto> getAllUsers(Pageable pageable, Boolean active, Boolean verified) {
        // Implementation
        return Page.empty();
    }

    public UserDetailDto getUserDetails(UUID id) {
        // Implementation
        return null;
    }

    public void suspendUser(UUID id, String reason) {
        // Implementation
    }

    public void activateUser(UUID id) {
        // Implementation
    }

    public void deleteUser(UUID id) {
        // Implementation
    }

    // ========== SYSTEM ==========

    public Page<ActivityLogDto> getPlatformLogs(Pageable pageable, String type) {
        // Implementation
        return Page.empty();
    }

    // ========== HELPER METHODS ==========

    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            password.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        return password.toString();
    }

    private AdminDto toAdminDto(User user) {
        return AdminDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .status(user.isActive() ? "ACTIVE" : "INACTIVE")
                .isActive(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }

    // ============================================================
// SUPER ADMIN PROFILE & COUNT
// ============================================================

    /**
     * Get Super Admin profile
     */
    public SuperAdminProfileDto getSuperAdminProfile(UUID userId) throws BusinessException {
        User superAdmin = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Super Admin not found"));

        if (superAdmin.getRole() != UserRole.SUPER_ADMIN) {
            throw new BusinessException("User is not a Super Admin");
        }

        // Get total Super Admins count
        long totalSuperAdmins = userRepository.countByRole(UserRole.SUPER_ADMIN);

        return SuperAdminProfileDto.builder()
                .id(superAdmin.getId())
                .email(superAdmin.getEmail())
                .fullName(superAdmin.getFullName())
                .phone(superAdmin.getPhone())
                .role(superAdmin.getRole())
                .avatarUrl(superAdmin.getAvatarUrl())
                .isActive(superAdmin.isActive())
                .createdAt(superAdmin.getCreatedAt())
                .totalSuperAdmins(totalSuperAdmins)
                .build();
    }

    /**
     * Get total Super Admins count
     */
    public long getSuperAdminCount() {
        return userRepository.countByRole(UserRole.SUPER_ADMIN);
    }
}