package com.gaslink.api.modules.vendor;
import com.gaslink.api.modules.vendor.dto.*;
import com.gaslink.api.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api/v1/vendors")

public class VendorController {
    private final VendorService vendorService;

    public VendorController(VendorService vendorService) {
        this.vendorService = vendorService;
    }

    @PostMapping("/register")
    @PreAuthorize("hasAnyRole('CUSTOMER','VENDOR')")
    public ResponseEntity<ApiResponse<VendorProfileDto>> register(Authentication auth,
            @Valid @RequestBody VendorRegistrationRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success("Vendor registered. Pending verification.", vendorService.register(UUID.fromString(auth.getName()), req)));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<VendorProfileDto>> getMyProfile(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(vendorService.getProfile(UUID.fromString(auth.getName()))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VendorProfileDto>> getProfile(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(vendorService.getProfile(id)));
    }

    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<List<NearbyVendorDto>>> getNearby(
            @RequestParam double lat, @RequestParam double lng,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(ApiResponse.success(vendorService.getNearbyVendors(lat, lng, limit)));
    }

    @PatchMapping("/me/open")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<VendorProfileDto>> toggleOpen(Authentication auth, @RequestParam boolean open) {
        return ResponseEntity.ok(ApiResponse.success(vendorService.toggleOpen(UUID.fromString(auth.getName()), open)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/verification")
    public ResponseEntity<ApiResponse<VendorProfileDto>> updateVerification(
            @PathVariable UUID id, @Valid @RequestBody UpdateVendorStatusRequest req) {
        return ResponseEntity.ok(ApiResponse.success(vendorService.updateVerificationStatus(id, req)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/disable")
    public ResponseEntity<ApiResponse<VendorProfileDto>> disableVendor(
            @PathVariable UUID id, @Valid @RequestBody VendorAccountActionRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Vendor account disabled.", vendorService.disableVendor(id, req.getReason())));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/enable")
    public ResponseEntity<ApiResponse<VendorProfileDto>> enableVendor(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Vendor account enabled.", vendorService.enableVendor(id)));
    }
}