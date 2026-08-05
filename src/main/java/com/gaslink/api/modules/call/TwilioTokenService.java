package com.gaslink.api.modules.call;

import com.twilio.jwt.accesstoken.AccessToken;
import com.twilio.jwt.accesstoken.VoiceGrant;
import com.gaslink.api.exception.BusinessException;
import com.gaslink.api.modules.order.Order;
import com.gaslink.api.modules.order.OrderRepository;
import com.gaslink.api.modules.user.User;
import com.gaslink.api.modules.user.UserRepository;
import com.gaslink.api.shared.enums.OrderStatus;
import com.gaslink.api.shared.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class TwilioTokenService {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.api-key}")
    private String apiKey;

    @Value("${twilio.api-secret}")
    private String apiSecret;

    @Value("${twilio.app-sid}")
    private String twimlAppSid;

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public TwilioTokenService(OrderRepository orderRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    /**
     * Generate a Twilio access token for in-app calling
     */
    public String generateAccessToken(UUID userId, UUID orderId) throws BusinessException {
        // Validate user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Validate order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Validate user is part of the order
        if (!order.getCustomerId().equals(userId) &&
                !(order.getVendorId() != null && order.getVendorId().equals(userId))) {
            throw new BusinessException("You are not part of this order");
        }

        // Validate order status - only allow for ACCEPTED, PROCESSING, READY
        if (order.getStatus() == OrderStatus.PENDING) {
            throw new BusinessException("Order has not been accepted yet. Please wait for the vendor to accept.");
        }

        if (order.getStatus() == OrderStatus.REJECTED ||
                order.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessException("Order is no longer active. Calls are not available.");
        }

        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new BusinessException("Order has been completed. Calls are no longer available.");
        }

        // Create Voice grant
        VoiceGrant voiceGrant = new VoiceGrant();
        voiceGrant.setOutgoingApplicationSid(twimlAppSid);
        voiceGrant.setIncomingAllow(true);

        // Create access token
        AccessToken token = new AccessToken.Builder(accountSid, apiKey, apiSecret)
                .identity(userId.toString())
                .grant(voiceGrant)
                .build();

        log.info("🔑 Access token generated for user: {} for order: {} (Status: {})",
                userId, orderId, order.getStatus());
        return token.toJwt();
    }

    /**
     * Get the room name for an order (both parties join the same room)
     */
    public String getRoomName(UUID orderId) {
        return "order-" + orderId.toString();
    }
}