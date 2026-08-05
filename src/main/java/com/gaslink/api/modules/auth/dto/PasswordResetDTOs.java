package com.gaslink.api.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class PasswordResetDTOs {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Forgot password request")
    public static class ForgotPasswordRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Schema(description = "User email", required = true, example = "user@example.com")
        private String email;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Verify reset token request")
    public static class VerifyTokenRequest {
        @NotBlank(message = "Token is required")
        @Schema(description = "Reset token", required = true)
        private String token;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Reset password request")
    public static class ResetPasswordRequest {
        @NotBlank(message = "Token is required")
        @Schema(description = "Reset token", required = true)
        private String token;

        @NotBlank(message = "New password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        @Schema(description = "New password", required = true, example = "NewSecurePass123!")
        private String newPassword;

        @NotBlank(message = "Confirm password is required")
        @Schema(description = "Confirm password", required = true, example = "NewSecurePass123!")
        private String confirmPassword;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Password reset response")
    public static class PasswordResetResponse {
        @Schema(description = "Success message")
        private String message;

        @Schema(description = "Email sent to")
        private String email;

        @Schema(description = "Token expiry in minutes")
        private Integer expiryMinutes;
    }
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        @Schema(description = "Set up password request (for new Super Admin)")
        public static class SetupPasswordRequest {
            @NotBlank(message = "Token is required")
            @Schema(description = "Setup token from email", required = true)
            private String token;

            @NotBlank(message = "Password is required")
            @Size(min = 8, message = "Password must be at least 8 characters")
            @Schema(description = "New password", required = true, example = "SecurePass123!")
            private String password;

            @NotBlank(message = "Confirm password is required")
            @Schema(description = "Confirm password", required = true, example = "SecurePass123!")
            private String confirmPassword;
        }
    }