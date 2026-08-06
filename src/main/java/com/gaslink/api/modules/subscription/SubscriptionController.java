package com.gaslink.api.modules.subscription;

import com.gaslink.api.exception.BusinessException;
import com.gaslink.api.modules.payment.dto.InitiatePaymentResponse;
import com.gaslink.api.modules.subscription.dto.*;
import com.gaslink.api.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
@Tag(name = "Subscription", description = "Subscription management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @Operation(
            summary = "Initiate subscription payment",
            description = "Start subscription payment process with Paystack"
    )
    @PostMapping("/initiate-payment")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<InitiatePaymentResponse>> initiateSubscriptionPayment(
            @Parameter(hidden = true) Authentication auth,
            @Valid @RequestBody InitiateSubscriptionPaymentRequest request) throws BusinessException {
        UUID vendorId = UUID.fromString(auth.getName());
        log.info("💳 Vendor {} initiating subscription payment for plan: {}", vendorId, request.getPlan());
        return ResponseEntity.ok(ApiResponse.ok(
                subscriptionService.initiateSubscriptionPayment(vendorId, request)
        ));
    }

    @Operation(
            summary = "Verify subscription payment",
            description = "Verify subscription payment after Paystack callback"
    )
    @PostMapping("/verify-payment")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<SubscriptionDto>> verifySubscriptionPayment(
            @Parameter(description = "Payment reference", required = true) @RequestParam String reference) throws BusinessException {
        log.info("✅ Verifying subscription payment: {}", reference);
        return ResponseEntity.ok(ApiResponse.ok(
                subscriptionService.verifySubscriptionPayment(reference)
        ));
    }

    @Operation(
            summary = "Check payment status",
            description = "Check the status of a subscription payment"
    )
    @GetMapping("/payment-status")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<PaymentStatusResponse>> getPaymentStatus(
            @Parameter(description = "Payment reference", required = true) @RequestParam String reference) throws BusinessException {
        log.info("🔍 Checking payment status for: {}", reference);
        return ResponseEntity.ok(ApiResponse.ok(
                subscriptionService.getPaymentStatus(reference)
        ));
    }

    @Operation(
            summary = "Get my current subscription",
            description = "Get the current subscription for the authenticated vendor"
    )
    @GetMapping("/me")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<SubscriptionDto>> getMySubscription(
            @Parameter(hidden = true) Authentication auth) {
        UUID vendorId = UUID.fromString(auth.getName());
        return ResponseEntity.ok(ApiResponse.ok(
                subscriptionService.getMySubscription(vendorId)
        ));
    }

    @Operation(
            summary = "Check if vendor has active subscription",
            description = "Check if a specific vendor has an active subscription"
    )
    @GetMapping("/active/{vendorId}")
    public ResponseEntity<ApiResponse<Boolean>> hasActiveSubscription(
            @Parameter(description = "Vendor ID", required = true) @PathVariable UUID vendorId) {
        return ResponseEntity.ok(ApiResponse.ok(
                subscriptionService.hasActiveSubscription(vendorId)
        ));
    }

    @Operation(
            summary = "Get all subscriptions (Admin only)",
            description = "Get all subscriptions for admin dashboard"
    )
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<SubscriptionDto>>> getAllSubscriptions() {
        return ResponseEntity.ok(ApiResponse.ok(
                subscriptionService.getAllSubscriptions()
        ));
    }
}