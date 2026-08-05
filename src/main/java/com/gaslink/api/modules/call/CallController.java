package com.gaslink.api.modules.call;

import com.gaslink.api.exception.BusinessException;
import com.gaslink.api.modules.call.dto.CallAvailabilityResponse;
import com.gaslink.api.modules.call.dto.CallTokenResponse;
import com.gaslink.api.modules.order.OrderRepository;
import com.gaslink.api.modules.order.Order;
import com.gaslink.api.shared.enums.OrderStatus;
import com.gaslink.api.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/calls")
@RequiredArgsConstructor
@Tag(name = "Calls", description = "In-app voice call management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class CallController {

    private final TwilioTokenService twilioTokenService;
    private final CallService callService;

    @Operation(
            summary = "Get Twilio access token for in-app call",
            description = "Generates a Twilio access token for the user to make/receive in-app voice calls. " +
                    "Only available for orders that have been accepted by the vendor."
    )
    @PostMapping("/token")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, String>>> getAccessToken(
            @Parameter(hidden = true) Authentication auth,
            @Parameter(description = "Order ID", required = true) @RequestParam UUID orderId) throws BusinessException {
        UUID userId = UUID.fromString(auth.getName());
        log.info("🔑 Generating access token for user: {} for order: {}", userId, orderId);

        // Validate order and user permissions
        validateOrderForCall(orderId, userId);

        String token = twilioTokenService.generateAccessToken(userId, orderId);
        String roomName = twilioTokenService.getRoomName(orderId);

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("roomName", roomName);
        response.put("identity", userId.toString());

        return ResponseEntity.ok(ApiResponse.ok("Access token generated successfully", response));
    }

    @Operation(
            summary = "Initiate a call",
            description = "Notify the other party that a call is being initiated. " +
                    "Only available for orders that have been accepted by the vendor."
    )
    @PostMapping("/initiate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, String>>> initiateCall(
            @Parameter(hidden = true) Authentication auth,
            @Parameter(description = "Order ID", required = true) @RequestParam UUID orderId) throws BusinessException {
        UUID callerId = UUID.fromString(auth.getName());
        log.info("📞 User {} initiating call for order: {}", callerId, orderId);

        // Validate order and user permissions
        validateOrderForCall(orderId, callerId);

        String token = twilioTokenService.generateAccessToken(callerId, orderId);
        String roomName = twilioTokenService.getRoomName(orderId);

        // Notify the other party about the incoming call
        callService.notifyIncomingCall(orderId, callerId);

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("roomName", roomName);

        return ResponseEntity.ok(ApiResponse.ok("Call initiated successfully", response));
    }

    @Operation(
            summary = "Join a call",
            description = "Join an ongoing call room. Only available for orders that have been accepted by the vendor."
    )
    @PostMapping("/join")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, String>>> joinCall(
            @Parameter(hidden = true) Authentication auth,
            @Parameter(description = "Order ID", required = true) @RequestParam UUID orderId) throws BusinessException {
        UUID userId = UUID.fromString(auth.getName());
        log.info("📞 User {} joining call for order: {}", userId, orderId);

        // Validate order and user permissions
        validateOrderForCall(orderId, userId);

        String token = twilioTokenService.generateAccessToken(userId, orderId);
        String roomName = twilioTokenService.getRoomName(orderId);

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("roomName", roomName);

        return ResponseEntity.ok(ApiResponse.ok("Joined call successfully", response));
    }

    @Operation(
            summary = "Check if call is available for an order",
            description = "Checks if the order is in a state where a call can be made (ACCEPTED or higher)"
    )
    @GetMapping("/available/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CallAvailabilityResponse>> isCallAvailable(
            @Parameter(hidden = true) Authentication auth,
            @Parameter(description = "Order ID", required = true) @PathVariable UUID orderId) {
        UUID userId = UUID.fromString(auth.getName());
        log.info("🔍 Checking call availability for user: {} order: {}", userId, orderId);

        CallAvailabilityResponse response = callService.checkCallAvailability(orderId, userId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(
            summary = "End a call",
            description = "End an ongoing call"
    )
    @PostMapping("/end")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> endCall(
            @Parameter(hidden = true) Authentication auth,
            @Parameter(description = "Order ID", required = true) @RequestParam UUID orderId) throws BusinessException {
        UUID userId = UUID.fromString(auth.getName());
        log.info("📞 User {} ending call for order: {}", userId, orderId);

        // Validate user is part of the order
        validateOrderForCall(orderId, userId);

        callService.endCall(orderId, userId);
        return ResponseEntity.ok(ApiResponse.ok("Call ended successfully", null));
    }

    @Operation(
            summary = "Reject an incoming call",
            description = "Reject an incoming call notification"
    )
    @PostMapping("/reject")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> rejectCall(
            @Parameter(hidden = true) Authentication auth,
            @Parameter(description = "Order ID", required = true) @RequestParam UUID orderId) throws BusinessException {
        UUID userId = UUID.fromString(auth.getName());
        log.info("📞 User {} rejecting call for order: {}", userId, orderId);

        // Validate user is part of the order
        validateOrderForCall(orderId, userId);

        callService.rejectCall(orderId, userId);
        return ResponseEntity.ok(ApiResponse.ok("Call rejected successfully", null));
    }

    /**
     * Validate that the order is in a state where a call can be made
     */
    private void validateOrderForCall(UUID orderId, UUID userId) throws BusinessException {
        Order order = callService.getOrder(orderId);

        // Check if user is part of the order
        if (!order.getCustomerId().equals(userId) &&
                (order.getVendorId() == null || !order.getVendorId().equals(userId))) {
            throw new BusinessException("You are not authorized to make calls for this order");
        }

        // Check if vendor is assigned
        if (order.getVendorId() == null) {
            throw new BusinessException("No vendor has accepted this order yet. Please wait for a vendor to accept.");
        }

        // Check order status - only allow calls for ACCEPTED, PROCESSING, READY orders
        if (order.getStatus() == OrderStatus.PENDING) {
            throw new BusinessException("This order has not been accepted yet. Please wait for the vendor to accept.");
        }

        if (order.getStatus() == OrderStatus.REJECTED) {
            throw new BusinessException("This order has been rejected. Calls are not available.");
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessException("This order has been cancelled. Calls are not available.");
        }

        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new BusinessException("This order has been completed. Calls are no longer available.");
        }

        log.info("✅ Order {} validated for call by user {}", orderId, userId);
    }
}