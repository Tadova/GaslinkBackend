package com.gaslink.api.modules.call;

import com.gaslink.api.exception.BusinessException;
import com.gaslink.api.modules.call.dto.CallAvailabilityResponse;
import com.gaslink.api.modules.notification.NotificationService;
import com.gaslink.api.modules.order.Order;
import com.gaslink.api.modules.order.OrderRepository;
import com.gaslink.api.modules.user.User;
import com.gaslink.api.modules.user.UserRepository;
import com.gaslink.api.shared.enums.OrderStatus;
import com.gaslink.api.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CallService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Get order by ID
     */
    public Order getOrder(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

    /**
     * Check if call is available for an order
     */
    public CallAvailabilityResponse checkCallAvailability(UUID orderId, UUID userId) {
        Order order = getOrder(orderId);

        // Check if user is part of the order
        boolean isCustomer = order.getCustomerId().equals(userId);
        boolean isVendor = order.getVendorId() != null && order.getVendorId().equals(userId);

        if (!isCustomer && !isVendor) {
            return CallAvailabilityResponse.builder()
                    .available(false)
                    .message("You are not authorized to make calls for this order")
                    .orderId(orderId)
                    .orderReference(order.getOrderReference())
                    .orderStatus(order.getStatus())
                    .build();
        }

        // Check if vendor is assigned
        if (order.getVendorId() == null) {
            return CallAvailabilityResponse.builder()
                    .available(false)
                    .message("No vendor has accepted this order yet")
                    .orderId(orderId)
                    .orderReference(order.getOrderReference())
                    .orderStatus(order.getStatus())
                    .build();
        }

        // Check order status
        if (order.getStatus() == OrderStatus.PENDING) {
            return CallAvailabilityResponse.builder()
                    .available(false)
                    .message("This order has not been accepted yet. Please wait for the vendor to accept.")
                    .orderId(orderId)
                    .orderReference(order.getOrderReference())
                    .orderStatus(order.getStatus())
                    .vendorId(order.getVendorId())
                    .customerId(order.getCustomerId())
                    .build();
        }

        if (order.getStatus() == OrderStatus.REJECTED ||
                order.getStatus() == OrderStatus.CANCELLED) {
            return CallAvailabilityResponse.builder()
                    .available(false)
                    .message("This order is no longer active. Calls are not available.")
                    .orderId(orderId)
                    .orderReference(order.getOrderReference())
                    .orderStatus(order.getStatus())
                    .build();
        }

        if (order.getStatus() == OrderStatus.COMPLETED) {
            return CallAvailabilityResponse.builder()
                    .available(false)
                    .message("This order has been completed. Calls are no longer available.")
                    .orderId(orderId)
                    .orderReference(order.getOrderReference())
                    .orderStatus(order.getStatus())
                    .build();
        }

        // Get vendor and customer names - FIX: Use String variables directly without lambda modification
        String vendorName = "Unknown Vendor";
        String customerName = "Unknown Customer";

        if (order.getVendorId() != null) {
            User vendor = userRepository.findById(order.getVendorId()).orElse(null);
            if (vendor != null) {
                vendorName = vendor.getFullName();
            }
        }

        User customer = userRepository.findById(order.getCustomerId()).orElse(null);
        if (customer != null) {
            customerName = customer.getFullName();
        }

        // Call is available for ACCEPTED, PROCESSING, READY statuses
        return CallAvailabilityResponse.builder()
                .available(true)
                .message("Call is available for this order")
                .orderId(orderId)
                .orderReference(order.getOrderReference())
                .orderStatus(order.getStatus())
                .vendorId(order.getVendorId())
                .vendorName(vendorName)
                .customerId(order.getCustomerId())
                .customerName(customerName)
                .build();
    }

    /**
     * Notify the other party about an incoming call
     */
    public void notifyIncomingCall(UUID orderId, UUID callerId) throws BusinessException {
        Order order = getOrder(orderId);

        User caller = userRepository.findById(callerId)
                .orElseThrow(() -> new ResourceNotFoundException("Caller not found"));

        // Determine who is receiving the call
        UUID receiverId;
        if (order.getCustomerId().equals(callerId)) {
            receiverId = order.getVendorId();
        } else if (order.getVendorId() != null && order.getVendorId().equals(callerId)) {
            receiverId = order.getCustomerId();
        } else {
            throw new BusinessException("You are not part of this order");
        }

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new ResourceNotFoundException("Receiver not found"));

        // Send push notification to receiver
        String title = "📞 Incoming Call";
        String body = caller.getFullName() + " is calling you for order #" + order.getOrderReference();

        Map<String, String> data = new HashMap<>();
        data.put("type", "INCOMING_CALL");
        data.put("orderId", orderId.toString());
        data.put("callerId", callerId.toString());
        data.put("callerName", caller.getFullName());
        data.put("orderReference", order.getOrderReference());

        // Send push notification
        if (receiver.getPushToken() != null && !receiver.getPushToken().isEmpty()) {
            notificationService.sendNotificationToUser(receiverId, title, body, data);
        }

        // Send real-time WebSocket notification
        Map<String, Object> wsMessage = new HashMap<>();
        wsMessage.put("type", "INCOMING_CALL");
        wsMessage.put("orderId", orderId);
        wsMessage.put("callerId", callerId);
        wsMessage.put("callerName", caller.getFullName());
        wsMessage.put("orderReference", order.getOrderReference());
        wsMessage.put("roomName", "order-" + orderId);

        messagingTemplate.convertAndSendToUser(
                receiverId.toString(),
                "/queue/calls",
                wsMessage
        );

        log.info("📞 Incoming call notification sent to: {}", receiverId);
    }

    /**
     * End a call
     */
    public void endCall(UUID orderId, UUID userId) {
        Order order = getOrder(orderId);

        // Get the other party
        UUID otherPartyId;
        if (order.getCustomerId().equals(userId)) {
            otherPartyId = order.getVendorId();
        } else {
            otherPartyId = order.getCustomerId();
        }

        // Send WebSocket notification to both parties
        Map<String, Object> message = new HashMap<>();
        message.put("type", "CALL_ENDED");
        message.put("orderId", orderId);
        message.put("endedBy", userId.toString());

        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/calls",
                message
        );

        if (otherPartyId != null) {
            messagingTemplate.convertAndSendToUser(
                    otherPartyId.toString(),
                    "/queue/calls",
                    message
            );
        }

        log.info("📞 Call ended for order: {} by user: {}", orderId, userId);
    }

    /**
     * Reject an incoming call
     */
    public void rejectCall(UUID orderId, UUID userId) {
        Order order = getOrder(orderId);

        UUID otherPartyId;
        if (order.getCustomerId().equals(userId)) {
            otherPartyId = order.getVendorId();
        } else {
            otherPartyId = order.getCustomerId();
        }

        if (otherPartyId != null) {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "CALL_REJECTED");
            message.put("orderId", orderId);
            message.put("rejectedBy", userId.toString());

            messagingTemplate.convertAndSendToUser(
                    otherPartyId.toString(),
                    "/queue/calls",
                    message
            );
        }

        log.info("📞 Call rejected for order: {} by user: {}", orderId, userId);
    }
}