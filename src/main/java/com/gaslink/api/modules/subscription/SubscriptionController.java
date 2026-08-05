package com.gaslink.api.modules.subscription;

import com.gaslink.api.exception.BusinessException;
import com.gaslink.api.modules.payment.dto.InitiatePaymentResponse;
import com.gaslink.api.modules.subscription.dto.CreateSubscriptionRequest;
import com.gaslink.api.modules.subscription.dto.SubscriptionDto;
import com.gaslink.api.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
@Tag(name = "Subscription", description = "Subscription management endpoints for vendor subscriptions")
@SecurityRequirement(name = "bearerAuth")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @Operation(
            summary = "Get my current subscription",
            description = "Retrieves the current subscription details for the authenticated vendor."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Subscription details retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Free Trial",
                                            value = """
                        {
                            "success": true,
                            "message": "Success",
                            "data": {
                                "id": "sub_550e8400-e29b-41d4-a716-446655440000",
                                "vendorId": "550e8400-e29b-41d4-a716-446655440000",
                                "plan": "FREE_TRIAL",
                                "amount": 0.00,
                                "billingCycle": "MONTHLY",
                                "status": "FREE_TRIAL",
                                "startedAt": "2026-08-05T10:00:00Z",
                                "expiresAt": "2026-09-04T10:00:00Z"
                            },
                            "timestamp": "2026-08-05T10:00:00Z"
                        }
                        """
                                    ),
                                    @ExampleObject(
                                            name = "BASIC (Monthly)",
                                            value = """
                        {
                            "success": true,
                            "message": "Success",
                            "data": {
                                "id": "sub_550e8400-e29b-41d4-a716-446655440000",
                                "vendorId": "550e8400-e29b-41d4-a716-446655440000",
                                "plan": "BASIC",
                                "amount": 5000.00,
                                "billingCycle": "MONTHLY",
                                "status": "ACTIVE",
                                "startedAt": "2026-08-05T10:00:00Z",
                                "expiresAt": "2026-09-05T10:00:00Z"
                            },
                            "timestamp": "2026-08-05T10:00:00Z"
                        }
                        """
                                    ),
                                    @ExampleObject(
                                            name = "PREMIUM (Annual)",
                                            value = """
                        {
                            "success": true,
                            "message": "Success",
                            "data": {
                                "id": "sub_550e8400-e29b-41d4-a716-446655440000",
                                "vendorId": "550e8400-e29b-41d4-a716-446655440000",
                                "plan": "PREMIUM",
                                "amount": 50000.00,
                                "billingCycle": "ANNUAL",
                                "status": "ACTIVE",
                                "startedAt": "2026-08-05T10:00:00Z",
                                "expiresAt": "2027-08-05T10:00:00Z"
                            },
                            "timestamp": "2026-08-05T10:00:00Z"
                        }
                        """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - User is not a VENDOR")
    })
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
            summary = "Initiate subscription payment",
            description = "Starts the subscription payment process. BASIC = Monthly (₦5,000), PREMIUM = Annual (₦50,000)."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Payment initiated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "BASIC (Monthly)",
                                            value = """
                        {
                            "success": true,
                            "message": "Success",
                            "data": {
                                "authorizationUrl": "https://paystack.com/pay/abcdef123456",
                                "reference": "SUB-550e8400-1700000000",
                                "accessCode": "access_code_12345"
                            },
                            "timestamp": "2026-08-05T10:00:00Z"
                        }
                        """
                                    ),
                                    @ExampleObject(
                                            name = "PREMIUM (Annual)",
                                            value = """
                        {
                            "success": true,
                            "message": "Success",
                            "data": {
                                "authorizationUrl": "https://paystack.com/pay/abcdef123456",
                                "reference": "SUB-550e8400-1700000001",
                                "accessCode": "access_code_12346"
                            },
                            "timestamp": "2026-08-05T10:00:00Z"
                        }
                        """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request - Invalid plan"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Vendor not verified"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @PostMapping("/initiate-payment")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<InitiatePaymentResponse>> initiateSubscriptionPayment(
            @Parameter(hidden = true) Authentication auth,
            @Valid @RequestBody CreateSubscriptionRequest request) throws BusinessException {
        UUID vendorId = UUID.fromString(auth.getName());
        return ResponseEntity.ok(ApiResponse.ok(
                subscriptionService.initiateSubscriptionPayment(vendorId, request)
        ));
    }

    @Operation(
            summary = "Confirm subscription payment",
            description = "Verifies the payment with Paystack and activates the subscription."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Payment confirmed and subscription activated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                    {
                        "success": true,
                        "message": "Success",
                        "data": {
                            "id": "sub_550e8400-e29b-41d4-a716-446655440000",
                            "vendorId": "550e8400-e29b-41d4-a716-446655440000",
                            "plan": "BASIC",
                            "amount": 5000.00,
                            "billingCycle": "MONTHLY",
                            "status": "ACTIVE",
                            "startedAt": "2026-08-05T10:00:00Z",
                            "expiresAt": "2026-09-05T10:00:00Z"
                        },
                        "timestamp": "2026-08-05T10:00:00Z"
                    }
                    """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Payment verification failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "No pending subscription found")
    })
    @PostMapping("/confirm-payment")
    public ResponseEntity<ApiResponse<SubscriptionDto>> confirmSubscriptionPayment(
            @Parameter(description = "Payment reference from initiate-payment", required = true)
            @RequestParam String reference) throws BusinessException {
        return ResponseEntity.ok(ApiResponse.ok(
                subscriptionService.confirmSubscriptionPayment(reference)
        ));
    }

    @Operation(
            summary = "Get all subscriptions (Admin only)",
            description = "Retrieves all subscriptions across all vendors. Only accessible by ADMIN and SUPER_ADMIN."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "All subscriptions retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                    {
                        "success": true,
                        "message": "Success",
                        "data": [
                            {
                                "id": "sub_1",
                                "vendorId": "vendor_1",
                                "plan": "BASIC",
                                "amount": 5000.00,
                                "billingCycle": "MONTHLY",
                                "status": "ACTIVE",
                                "startedAt": "2026-08-05T10:00:00Z",
                                "expiresAt": "2026-09-05T10:00:00Z"
                            },
                            {
                                "id": "sub_2",
                                "vendorId": "vendor_2",
                                "plan": "PREMIUM",
                                "amount": 50000.00,
                                "billingCycle": "ANNUAL",
                                "status": "ACTIVE",
                                "startedAt": "2026-07-01T10:00:00Z",
                                "expiresAt": "2027-07-01T10:00:00Z"
                            }
                        ],
                        "timestamp": "2026-08-05T10:00:00Z"
                    }
                    """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN or SUPER_ADMIN role")
    })
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<SubscriptionDto>>> getAllSubscriptions() {
        return ResponseEntity.ok(ApiResponse.ok(
                subscriptionService.getAllSubscriptions()
        ));
    }

    @Operation(
            summary = "Check if vendor has active subscription",
            description = "Public endpoint to check if a specific vendor has an active subscription."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Subscription status checked successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Active",
                                            value = """
                        {
                            "success": true,
                            "message": "Success",
                            "data": true,
                            "timestamp": "2026-08-05T10:00:00Z"
                        }
                        """
                                    ),
                                    @ExampleObject(
                                            name = "Inactive",
                                            value = """
                        {
                            "success": true,
                            "message": "Success",
                            "data": false,
                            "timestamp": "2026-08-05T10:00:00Z"
                        }
                        """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Vendor not found")
    })
    @GetMapping("/active/{vendorId}")
    public ResponseEntity<ApiResponse<Boolean>> hasActiveSubscription(
            @Parameter(description = "Vendor UUID to check", required = true)
            @PathVariable UUID vendorId) {
        return ResponseEntity.ok(ApiResponse.ok(
                subscriptionService.hasActiveSubscription(vendorId)
        ));
    }
}