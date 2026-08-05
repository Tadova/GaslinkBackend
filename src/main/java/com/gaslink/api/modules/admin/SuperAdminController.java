package com.gaslink.api.modules.admin;

import com.gaslink.api.exception.BusinessException;
import com.gaslink.api.modules.admin.dto.*;
import com.gaslink.api.modules.user.dto.UserActionRequest;
import com.gaslink.api.modules.user.dto.UserDetailDto;
import com.gaslink.api.modules.user.dto.UserInfoDto;
import com.gaslink.api.modules.vendor.dto.*;
import com.gaslink.api.response.ApiResponse;
import com.gaslink.api.util.ActivityLogDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;

import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/super-admin")
@PreAuthorize("hasRole('SUPER_ADMIN')")
@Tag(name = "Super Admin", description = "Super Admin management endpoints - Full platform control")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class SuperAdminController {

    private final SuperAdminService superAdminService;

    // ============================================================
    // SUPER ADMIN PROFILE
    // ============================================================

    @Operation(
            summary = "Get Super Admin profile",
            description = "Retrieves the profile of the authenticated Super Admin and total Super Admins count"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Profile retrieved successfully",
                    content = @Content(
                            examples = @ExampleObject(
                                    value = """
                                    {
                                        "success": true,
                                        "message": "Super Admin profile retrieved successfully",
                                        "data": {
                                            "id": "550e8400-e29b-41d4-a716-446655440000",
                                            "email": "superadmin@gaslink.com",
                                            "fullName": "Super Admin",
                                            "phone": "+2348012345678",
                                            "role": "SUPER_ADMIN",
                                            "avatarUrl": "data:image/jpeg;base64,/9j/4AAQSkZJRg...",
                                            "isActive": true,
                                            "createdAt": "2026-08-01T10:00:00Z",
                                            "totalSuperAdmins": 3
                                        },
                                        "timestamp": "2026-08-05T10:00:00Z"
                                    }
                                    """
                            )
                    )
            )
    })
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<SuperAdminProfileDto>> getSuperAdminProfile(
            @Parameter(hidden = true) Authentication auth) throws BusinessException {
        UUID userId = UUID.fromString(auth.getName());
        log.info("👑 Super Admin fetching their profile: {}", userId);
        return ResponseEntity.ok(ApiResponse.ok(
                "Super Admin profile retrieved successfully",
                superAdminService.getSuperAdminProfile(userId)
        ));
    }

    @Operation(
            summary = "Get total Super Admins count",
            description = "Returns the total number of Super Admins in the system"
    )
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> getSuperAdminCount() {
        log.info("👑 Fetching total Super Admins count");
        return ResponseEntity.ok(ApiResponse.ok(
                superAdminService.getSuperAdminCount()
        ));
    }

    // ============================================================
    // DASHBOARD & ANALYTICS
    // ============================================================

    @Operation(
            summary = "Get platform analytics dashboard",
            description = "Get comprehensive platform analytics including users, vendors, orders, and revenue statistics"
    )
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AnalyticsDto>> getDashboardAnalytics() {
        log.info("Super Admin fetching dashboard analytics");
        return ResponseEntity.ok(ApiResponse.ok(
                "Analytics retrieved successfully",
                superAdminService.getPlatformAnalytics()
        ));
    }

    @Operation(
            summary = "Get user analytics",
            description = "Get detailed user analytics and statistics"
    )
    @GetMapping("/analytics/users")
    public ResponseEntity<ApiResponse<UserStats>> getUserAnalytics() {
        log.info("Super Admin fetching user analytics");
        return ResponseEntity.ok(ApiResponse.ok(
                superAdminService.getUserAnalytics()
        ));
    }

    @Operation(
            summary = "Get vendor analytics",
            description = "Get detailed vendor analytics and statistics"
    )
    @GetMapping("/analytics/vendors")
    public ResponseEntity<ApiResponse<VendorStats>> getVendorAnalytics() {
        log.info("Super Admin fetching vendor analytics");
        return ResponseEntity.ok(ApiResponse.ok(
                superAdminService.getVendorAnalytics()
        ));
    }

    @Operation(
            summary = "Get order analytics",
            description = "Get detailed order analytics and statistics"
    )
    @GetMapping("/analytics/orders")
    public ResponseEntity<ApiResponse<OrderStats>> getOrderAnalytics() {
        log.info("Super Admin fetching order analytics");
        return ResponseEntity.ok(ApiResponse.ok(
                superAdminService.getOrderAnalytics()
        ));
    }

    @Operation(
            summary = "Get revenue analytics",
            description = "Get detailed revenue analytics"
    )
    @GetMapping("/analytics/revenue")
    public ResponseEntity<ApiResponse<RevenueStats>> getRevenueAnalytics() {
        log.info("Super Admin fetching revenue analytics");
        return ResponseEntity.ok(ApiResponse.ok(
                superAdminService.getRevenueAnalytics()
        ));
    }

    // ============================================================
    // ADMIN MANAGEMENT
    // ============================================================

    @Operation(
            summary = "Get all admins",
            description = "Retrieve all admin users with pagination and filtering"
    )
    @GetMapping("/admins")
    public ResponseEntity<ApiResponse<Page<AdminDto>>> getAllAdmins(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Filter by admin role: SUPER_ADMIN, ADMIN, SUPPORT")
            @RequestParam(required = false) String role,
            @Parameter(description = "Filter by status: ACTIVE, INACTIVE, SUSPENDED")
            @RequestParam(required = false) String status) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.ok(
                superAdminService.getAllAdmins(pageRequest, role, status)
        ));
    }

    @Operation(
            summary = "Get admin by ID",
            description = "Retrieve detailed admin information"
    )
    @GetMapping("/admins/{id}")
    public ResponseEntity<ApiResponse<AdminDto>> getAdminById(
            @Parameter(description = "Admin UUID", required = true)
            @PathVariable UUID id) throws BusinessException {
        return ResponseEntity.ok(ApiResponse.ok(
                superAdminService.getAdminById(id)
        ));
    }

    @Operation(
            summary = "Create a new admin",
            description = "Create a new admin user with specific role"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Admin created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error or user already exists")
    })
    @PostMapping("/admins")
    public ResponseEntity<ApiResponse<AdminDto>> createAdmin(
            @Valid @RequestBody CreateAdminRequest request) throws BusinessException {
        log.info("Super Admin creating new admin: {}", request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(
                        "Admin created successfully",
                        superAdminService.createAdmin(request)
                ));
    }

    @Operation(
            summary = "Update admin",
            description = "Update admin information, role, or status"
    )
    @PutMapping("/admins/{id}")
    public ResponseEntity<ApiResponse<AdminDto>> updateAdmin(
            @Parameter(description = "Admin UUID", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAdminRequest request) throws BusinessException {
        log.info("Super Admin updating admin: {}", id);
        return ResponseEntity.ok(ApiResponse.ok(
                "Admin updated successfully",
                superAdminService.updateAdmin(id, request)
        ));
    }

    @Operation(
            summary = "Delete admin",
            description = "Soft delete an admin user (cannot delete the last Super Admin)"
    )
    @DeleteMapping("/admins/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAdmin(
            @Parameter(description = "Admin UUID", required = true)
            @PathVariable UUID id) throws BusinessException {
        log.info("Super Admin deleting admin: {}", id);
        superAdminService.deleteAdmin(id);
        return ResponseEntity.ok(ApiResponse.ok(
                "Admin deleted successfully", null
        ));
    }

    @Operation(
            summary = "Suspend admin",
            description = "Suspend an admin account temporarily"
    )
    @PatchMapping("/admins/{id}/suspend")
    public ResponseEntity<ApiResponse<Void>> suspendAdmin(
            @Parameter(description = "Admin UUID", required = true)
            @PathVariable UUID id) throws BusinessException {
        log.info("Super Admin suspending admin: {}", id);
        superAdminService.suspendAdmin(id);
        return ResponseEntity.ok(ApiResponse.ok(
                "Admin suspended successfully", null
        ));
    }

    @Operation(
            summary = "Activate admin",
            description = "Activate a suspended admin account"
    )
    @PatchMapping("/admins/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> activateAdmin(
            @Parameter(description = "Admin UUID", required = true)
            @PathVariable UUID id) throws BusinessException {
        log.info("Super Admin activating admin: {}", id);
        superAdminService.activateAdmin(id);
        return ResponseEntity.ok(ApiResponse.ok(
                "Admin activated successfully", null
        ));
    }

    // ============================================================
    // VENDOR MANAGEMENT
    // ============================================================

    @Operation(
            summary = "Get all vendors",
            description = "Retrieve all vendors with detailed information"
    )
    @GetMapping("/vendors")
    public ResponseEntity<ApiResponse<Page<VendorInfoDto>>> getAllVendors(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Filter by status: PENDING, VERIFIED, REJECTED, SUSPENDED")
            @RequestParam(required = false) String status,
            @Parameter(description = "Filter by verification status")
            @RequestParam(required = false) Boolean verified) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.ok(
                superAdminService.getAllVendors(pageRequest, status, verified)
        ));
    }

    @Operation(
            summary = "Get vendor details",
            description = "Get detailed vendor information including analytics"
    )
    @GetMapping("/vendors/{id}")
    public ResponseEntity<ApiResponse<VendorDetailDto>> getVendorDetails(
            @Parameter(description = "Vendor UUID", required = true)
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                superAdminService.getVendorDetails(id)
        ));
    }

    @Operation(
            summary = "Get pending vendor applications",
            description = "Get all vendor applications pending verification"
    )
    @GetMapping("/vendors/pending")
    public ResponseEntity<ApiResponse<List<VendorApplicationDto>>> getPendingVendors() {
        return ResponseEntity.ok(ApiResponse.ok(
                superAdminService.getPendingVendorApplications()
        ));
    }

    @Operation(
            summary = "Verify vendor",
            description = "Verify or reject vendor application with reason"
    )
    @PostMapping("/vendors/{id}/verify")
    public ResponseEntity<ApiResponse<Void>> verifyVendor(
            @Parameter(description = "Vendor UUID", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody VendorVerificationRequest request) {
        log.info("Super Admin verifying vendor: {} with status: {}", id, request.getStatus());
        superAdminService.verifyVendor(id, request);
        return ResponseEntity.ok(ApiResponse.ok(
                "Vendor " + request.getStatus().toLowerCase() + " successfully", null
        ));
    }

    @Operation(
            summary = "Suspend vendor",
            description = "Suspend a vendor account with reason"
    )
    @PostMapping("/vendors/{id}/suspend")
    public ResponseEntity<ApiResponse<Void>> suspendVendor(
            @Parameter(description = "Vendor UUID", required = true)
            @PathVariable UUID id,
            @RequestBody VendorActionRequest request) {
        log.info("Super Admin suspending vendor: {}", id);
        superAdminService.suspendVendor(id, request.getReason());
        return ResponseEntity.ok(ApiResponse.ok(
                "Vendor suspended successfully", null
        ));
    }

    @Operation(
            summary = "Unsuspend vendor",
            description = "Unsuspend a vendor account"
    )
    @PostMapping("/vendors/{id}/unsuspend")
    public ResponseEntity<ApiResponse<Void>> unsuspendVendor(
            @Parameter(description = "Vendor UUID", required = true)
            @PathVariable UUID id) {
        log.info("Super Admin unsuspending vendor: {}", id);
        superAdminService.unsuspendVendor(id);
        return ResponseEntity.ok(ApiResponse.ok(
                "Vendor unsuspended successfully", null
        ));
    }

    @Operation(
            summary = "Delete vendor",
            description = "Permanently delete a vendor account (warning: irreversible)"
    )
    @DeleteMapping("/vendors/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVendor(
            @Parameter(description = "Vendor UUID", required = true)
            @PathVariable UUID id) {
        log.info("Super Admin deleting vendor: {}", id);
        superAdminService.deleteVendor(id);
        return ResponseEntity.ok(ApiResponse.ok(
                "Vendor deleted successfully", null
        ));
    }

    // ============================================================
    // USER MANAGEMENT
    // ============================================================

    @Operation(
            summary = "Get all users",
            description = "Retrieve all platform users with filtering"
    )
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<UserInfoDto>>> getAllUsers(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Filter by active status")
            @RequestParam(required = false) Boolean active,
            @Parameter(description = "Filter by verification status")
            @RequestParam(required = false) Boolean verified) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.ok(
                superAdminService.getAllUsers(pageRequest, active, verified)
        ));
    }

    @Operation(
            summary = "Get user details",
            description = "Get detailed user information"
    )
    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserDetailDto>> getUserDetails(
            @Parameter(description = "User UUID", required = true)
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                superAdminService.getUserDetails(id)
        ));
    }

    @Operation(
            summary = "Suspend user",
            description = "Suspend a user account"
    )
    @PostMapping("/users/{id}/suspend")
    public ResponseEntity<ApiResponse<Void>> suspendUser(
            @Parameter(description = "User UUID", required = true)
            @PathVariable UUID id,
            @RequestBody UserActionRequest request) {
        log.info("Super Admin suspending user: {}", id);
        superAdminService.suspendUser(id, request.getReason());
        return ResponseEntity.ok(ApiResponse.ok(
                "User suspended successfully", null
        ));
    }

    @Operation(
            summary = "Activate user",
            description = "Activate a suspended user account"
    )
    @PostMapping("/users/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> activateUser(
            @Parameter(description = "User UUID", required = true)
            @PathVariable UUID id) {
        log.info("Super Admin activating user: {}", id);
        superAdminService.activateUser(id);
        return ResponseEntity.ok(ApiResponse.ok(
                "User activated successfully", null
        ));
    }

    @Operation(
            summary = "Delete user",
            description = "Permanently delete a user account"
    )
    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @Parameter(description = "User UUID", required = true)
            @PathVariable UUID id) {
        log.info("Super Admin deleting user: {}", id);
        superAdminService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.ok(
                "User deleted successfully", null
        ));
    }

    // ============================================================
    // SYSTEM MANAGEMENT
    // ============================================================

    @Operation(
            summary = "Get platform logs",
            description = "Get recent platform activity logs"
    )
    @GetMapping("/logs")
    public ResponseEntity<ApiResponse<Page<ActivityLogDto>>> getPlatformLogs(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "50") int size,
            @Parameter(description = "Filter by log type: USER, VENDOR, ORDER, PAYMENT, ADMIN")
            @RequestParam(required = false) String type) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.ok(
                superAdminService.getPlatformLogs(pageRequest, type)
        ));
    }

    @Operation(
            summary = "Health check",
            description = "Check if Super Admin service is running"
    )
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.ok(
                "Super Admin service is running"
        ));
    }
}