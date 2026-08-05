package com.gaslink.api.modules.user;

import com.gaslink.api.exception.BusinessException;
import com.gaslink.api.modules.user.dto.UpdateProfileRequest;
import com.gaslink.api.modules.user.dto.UserProfileDto;
import com.gaslink.api.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
// REMOVE this import: import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "User management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "Get current user profile",
            description = "Retrieves the profile of the authenticated user"
    )
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserProfileDto>> getProfile(
            @Parameter(hidden = true) Authentication auth) {
        UUID userId = UUID.fromString(auth.getName());
        return ResponseEntity.ok(ApiResponse.ok(userService.getProfile(userId)));
    }

    @Operation(
            summary = "Update user profile",
            description = "Update user profile information. Email cannot be changed. Avatar must be base64 (max 1MB)."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Profile updated successfully",
                    content = @Content(
                            examples = {
                                    @ExampleObject(
                                            name = "Customer Update",
                                            value = """
                                            {
                                                "success": true,
                                                "message": "Profile updated successfully",
                                                "data": {
                                                    "id": "550e8400-e29b-41d4-a716-446655440000",
                                                    "fullName": "John Doe Updated",
                                                    "phone": "+2348012345678",
                                                    "email": "john@example.com",
                                                    "role": "CUSTOMER",
                                                    "avatarUrl": "data:image/jpeg;base64,/9j/4AAQSkZJRg..."
                                                },
                                                "timestamp": "2026-08-05T10:00:00Z"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Vendor Update",
                                            value = """
                                            {
                                                "success": true,
                                                "message": "Profile updated successfully",
                                                "data": {
                                                    "id": "550e8400-e29b-41d4-a716-446655440000",
                                                    "fullName": "Jane Smith",
                                                    "phone": "+2348012345678",
                                                    "email": "jane@example.com",
                                                    "role": "VENDOR",
                                                    "avatarUrl": "data:image/jpeg;base64,/9j/4AAQSkZJRg..."
                                                },
                                                "timestamp": "2026-08-05T10:00:00Z"
                                            }
                                            """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserProfileDto>> updateProfile(
            @Parameter(hidden = true) Authentication auth,
            @Valid @RequestBody UpdateProfileRequest request) throws BusinessException {
        UUID userId = UUID.fromString(auth.getName());
        log.info("👤 User {} updating profile", userId);
        return ResponseEntity.ok(ApiResponse.ok(
                "Profile updated successfully",
                userService.updateProfile(userId, request)
        ));
    }

    @Operation(
            summary = "Update vendor profile (Admin only)",
            description = "Admin can update vendor-specific fields"
    )
    @PutMapping("/vendor/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updateVendorProfile(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Valid @RequestBody UpdateProfileRequest request) throws BusinessException {
        log.info("🛒 Admin updating vendor profile for user: {}", userId);
        userService.updateVendorProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.ok("Vendor profile updated successfully", null));
    }
}