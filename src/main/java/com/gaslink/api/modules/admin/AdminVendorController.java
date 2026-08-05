package com.gaslink.api.modules.admin;

import com.gaslink.api.exception.BusinessException;
import com.gaslink.api.modules.vendor.dto.VendorStatisticsDto;
import com.gaslink.api.modules.admin.service.VendorManagementService;
import com.gaslink.api.modules.vendor.PendingVendorApplication;
import com.gaslink.api.modules.vendor.dto.VendorActionRequest;
import com.gaslink.api.modules.vendor.dto.VendorDetailResponse;
import com.gaslink.api.modules.vendor.dto.VendorListResponse;
import com.gaslink.api.modules.vendor.dto.VendorVerificationRequest;
import com.gaslink.api.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/vendors")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
@Tag(name = "Admin - Vendor Management", description = "Admin vendor management endpoints (accessible by ADMIN and SUPER_ADMIN)")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class AdminVendorController {

    private final VendorManagementService vendorManagementService;

    @Operation(
            summary = "Get all vendors",
            description = "Retrieve all vendors with pagination and filtering"
    )
    @GetMapping
    public ResponseEntity<ApiResponse<Page<VendorListResponse>>> getAllVendors(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Filter by status: PENDING, VERIFIED, REJECTED")
            @RequestParam(required = false) String status) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.ok(
                vendorManagementService.getAllVendors(pageRequest, status, null)
        ));
    }

    @Operation(
            summary = "Get pending vendor applications",
            description = "Get all vendor applications pending verification"
    )
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<PendingVendorApplication>>> getPendingVendors() {
        return ResponseEntity.ok(ApiResponse.ok(
                vendorManagementService.getPendingVendorApplications()
        ));
    }

    @Operation(
            summary = "Get vendor details",
            description = "Get detailed vendor information"
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VendorDetailResponse>> getVendorDetails(
            @Parameter(description = "Vendor UUID", required = true)
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                vendorManagementService.getVendorDetails(id)
        ));
    }

    @Operation(
            summary = "Verify vendor",
            description = "Verify or reject a vendor application"
    )
    @PostMapping("/{id}/verify")
    public ResponseEntity<ApiResponse<Void>> verifyVendor(
            @Parameter(description = "Vendor UUID", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody VendorVerificationRequest request) throws BusinessException {
        log.info("Admin verifying vendor: {} with status: {}", id, request.getStatus());
        vendorManagementService.verifyVendor(id, request);
        return ResponseEntity.ok(ApiResponse.ok(
                "Vendor " + request.getStatus().toLowerCase() + " successfully", null
        ));
    }

    @Operation(
            summary = "Suspend vendor",
            description = "Suspend a vendor account"
    )
    @PostMapping("/{id}/suspend")
    public ResponseEntity<ApiResponse<Void>> suspendVendor(
            @Parameter(description = "Vendor UUID", required = true)
            @PathVariable UUID id,
            @RequestBody VendorActionRequest request) {
        log.info("Admin suspending vendor: {}", id);
        vendorManagementService.suspendVendor(id, request.getReason());
        return ResponseEntity.ok(ApiResponse.ok(
                "Vendor suspended successfully", null
        ));
    }

    @Operation(
            summary = "Unsuspend vendor",
            description = "Unsuspend a vendor account"
    )
    @PostMapping("/{id}/unsuspend")
    public ResponseEntity<ApiResponse<Void>> unsuspendVendor(
            @Parameter(description = "Vendor UUID", required = true)
            @PathVariable UUID id) {
        log.info("Admin unsuspending vendor: {}", id);
        vendorManagementService.unsuspendVendor(id);
        return ResponseEntity.ok(ApiResponse.ok(
                "Vendor unsuspended successfully", null
        ));
    }

    @Operation(
            summary = "Delete vendor",
            description = "Permanently delete a vendor account (Super Admin only)"
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteVendor(
            @Parameter(description = "Vendor UUID", required = true)
            @PathVariable UUID id) {
        log.info("Super Admin deleting vendor: {}", id);
        vendorManagementService.deleteVendor(id);
        return ResponseEntity.ok(ApiResponse.ok(
                "Vendor deleted successfully", null
        ));
    }

    @Operation(
            summary = "Get vendor statistics",
            description = "Get vendor statistics summary"
    )
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<VendorStatisticsDto>> getVendorStatistics() {
        return ResponseEntity.ok(ApiResponse.ok(
                vendorManagementService.getVendorStatistics()
        ));
    }
}