package com.gaslink.api.modules.auth;

import com.gaslink.api.exception.BusinessException;
import com.gaslink.api.modules.auth.dto.AuthResponse;
import com.gaslink.api.modules.auth.dto.*;
import com.gaslink.api.response.ApiResponse; // Your custom response class
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
// REMOVE this import: import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Authentication and authorization endpoints")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
            summary = "Register a new user",
            description = "Register as a CUSTOMER or VENDOR. After registration, OTP will be sent to email."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Registration successful",
                    content = @Content(
                            examples = {
                                    @ExampleObject(
                                            name = "Customer Registration",
                                            value = """
                                            {
                                                "success": true,
                                                "message": "Registration successful. Please verify your OTP.",
                                                "data": {
                                                    "role": "CUSTOMER",
                                                    "fullName": "John Doe",
                                                    "email": "john@example.com"
                                                },
                                                "timestamp": "2026-08-05T10:00:00Z"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Vendor Registration",
                                            value = """
                                            {
                                                "success": true,
                                                "message": "Registration successful. Please verify your OTP.",
                                                "data": {
                                                    "role": "VENDOR",
                                                    "fullName": "Jane Smith",
                                                    "email": "jane@example.com"
                                                },
                                                "timestamp": "2026-08-05T10:00:00Z"
                                            }
                                            """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid role or user already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest req) throws BusinessException {
        authService.register(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Registration successful. Please verify your OTP.", null));
    }

    @Operation(
            summary = "Verify OTP",
            description = "Verify the OTP sent to user's phone/email to activate the account"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "OTP verified successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid or expired OTP"
            )
    })
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<Void>> verifyOtp(@Valid @RequestBody OtpVerifyRequest req) throws BusinessException {
        authService.verifyOtp(req);
        return ResponseEntity.ok(ApiResponse.ok("OTP verified successfully.", null));
    }

    @Operation(
            summary = "Login",
            description = "Login with phone or email"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(
                            examples = @ExampleObject(
                                    value = """
                                        {
                                            "success": true,
                                            "message": "Login successful",
                                            "data": {
                                                "accessToken": "eyJhbGciOiJIUzI1NiIs...",
                                                "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
                                                "tokenType": "Bearer",
                                                "expiresIn": 86400,
                                                "user": {
                                                    "id": "550e8400-e29b-41d4-a716-446655440000",
                                                    "email": "user@example.com",
                                                    "fullName": "John Doe",
                                                    "phone": "+2348012345678",
                                                    "role": "CUSTOMER"
                                                }
                                            }
                                        }
                                        """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Account not verified")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(authService.login(req)));
    }

    @Operation(
            summary = "Refresh token",
            description = "Get a new access token using refresh token"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid refresh token")
    })
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(authService.refreshToken(req)));
    }

    @Operation(
            summary = "Logout",
            description = "Invalidate the current user's session"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Logged out successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(Authentication auth) {
        authService.logout(UUID.fromString(auth.getName()));
        return ResponseEntity.ok(ApiResponse.ok("Logged out.", null));
    }

    @Operation(
            summary = "Forgot password",
            description = "Request a password reset link to be sent to your email"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Reset link sent successfully",
                    content = @Content(
                            examples = @ExampleObject(
                                    value = """
                                        {
                                            "success": true,
                                            "message": "Password reset link sent to your email",
                                            "data": {
                                                "email": "user@example.com",
                                                "expiryMinutes": 30
                                            }
                                        }
                                        """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid email format")
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<PasswordResetDTOs.PasswordResetResponse>> forgotPassword(
            @Valid @RequestBody PasswordResetDTOs.ForgotPasswordRequest request) throws BusinessException {
        return ResponseEntity.ok(ApiResponse.ok(
                authService.forgotPassword(request)));
    }

    @Operation(
            summary = "Verify reset token",
            description = "Verify if a reset token is valid before resetting password"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token is valid"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid or expired token")
    })
    @PostMapping("/verify-reset-token")
    public ResponseEntity<ApiResponse<Void>> verifyResetToken(
            @Valid @RequestBody PasswordResetDTOs.VerifyTokenRequest request) throws BusinessException {
        authService.verifyResetToken(request);
        return ResponseEntity.ok(ApiResponse.ok("Token is valid", null));
    }

    @Operation(
            summary = "Reset password",
            description = "Reset password using the token received in email"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Password reset successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid token or passwords don't match"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody PasswordResetDTOs.ResetPasswordRequest request) throws BusinessException {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.ok("Password reset successfully", null));
    }

    @Operation(
            summary = "Set up password (for new Super Admin)",
            description = "Set up password using the token from welcome email"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Password set up successfully. You can now login."
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid token or passwords don't match"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("/setup-password")
    public ResponseEntity<ApiResponse<Void>> setupPassword(
            @Valid @RequestBody PasswordResetDTOs.SetupPasswordRequest request) throws BusinessException {
        authService.setupPassword(request);
        return ResponseEntity.ok(ApiResponse.ok("Password set up successfully. You can now login.", null));
    }
}