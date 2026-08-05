package com.gaslink.api.modules.message.service;

import com.gaslink.api.exception.BusinessException;
import com.gaslink.api.modules.message.Message;
import com.gaslink.api.modules.message.MessageRepository;
import com.gaslink.api.modules.message.dto.MessageDto;
import com.gaslink.api.modules.message.dto.SendMessageRequest;
import com.gaslink.api.modules.order.Order;
import com.gaslink.api.modules.order.OrderRepository;
import com.gaslink.api.shared.enums.MessageType;
import com.gaslink.api.shared.enums.UserRole;
import com.gaslink.api.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final OrderRepository orderRepository;
    private final SimpMessagingTemplate messagingTemplate;

    private static final long MAX_IMAGE_SIZE_BYTES = 1_000_000; // 1MB
    private static final long MIN_IMAGE_SIZE_BYTES = 1_000; // 1KB

    /**
     * Validate and process base64 image
     */
    private String validateAndProcessImage(String imageBase64) throws BusinessException {
        if (imageBase64 == null || imageBase64.isEmpty()) {
            return null;
        }

        try {
            // Remove data URL prefix if present
            String base64Data = imageBase64;
            if (imageBase64.contains(",")) {
                base64Data = imageBase64.substring(imageBase64.indexOf(",") + 1);
            }

            // Check if there's actual data
            if (base64Data.trim().isEmpty()) {
                throw new BusinessException("Invalid image format: No image data found");
            }

            // Decode base64 to bytes
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);

            // Check minimum size (1KB - to ensure it's a real image)
            if (imageBytes.length < MIN_IMAGE_SIZE_BYTES) {
                throw new BusinessException("Image seems too small (minimum 1KB). Please upload a valid image.");
            }

            // Check maximum size (1MB)
            if (imageBytes.length > MAX_IMAGE_SIZE_BYTES) {
                throw new BusinessException("Image size must be less than 1MB. Current size: " +
                        (imageBytes.length / 1024) + "KB");
            }

            // Validate image format by checking magic bytes
            if (!isValidImageFormat(imageBytes)) {
                throw new BusinessException("Invalid image format. Please upload a valid JPEG, PNG, or GIF image.");
            }

            // Return the original base64 string
            return imageBase64;

        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid base64 image format. Please ensure the image is properly encoded.");
        }
    }

    /**
     * Validate image format by checking magic bytes
     */
    private boolean isValidImageFormat(byte[] imageBytes) {
        if (imageBytes.length < 4) {
            return false;
        }

        // Check for JPEG (FF D8 FF)
        if (imageBytes[0] == (byte) 0xFF && imageBytes[1] == (byte) 0xD8 && imageBytes[2] == (byte) 0xFF) {
            return true;
        }

        // Check for PNG (89 50 4E 47)
        if (imageBytes[0] == (byte) 0x89 && imageBytes[1] == (byte) 0x50 &&
                imageBytes[2] == (byte) 0x4E && imageBytes[3] == (byte) 0x47) {
            return true;
        }

        // Check for GIF (47 49 46 38)
        if (imageBytes[0] == (byte) 0x47 && imageBytes[1] == (byte) 0x49 &&
                imageBytes[2] == (byte) 0x46 && imageBytes[3] == (byte) 0x38) {
            return true;
        }

        // Check for WebP (52 49 46 46)
        if (imageBytes[0] == (byte) 0x52 && imageBytes[1] == (byte) 0x49 &&
                imageBytes[2] == (byte) 0x46 && imageBytes[3] == (byte) 0x46) {
            return true;
        }

        return false;
    }

    /**
     * Send a message
     */
    @Transactional
    public MessageDto sendMessage(UUID senderId, SendMessageRequest request) throws BusinessException {
        // Validate order exists
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Validate sender is part of the order
        if (!order.getCustomerId().equals(senderId) && !order.getVendorId().equals(senderId)) {
            throw new BusinessException("You are not authorized to send messages for this order");
        }

        // Determine receiver
        UUID receiverId;
        if (order.getCustomerId().equals(senderId)) {
            receiverId = order.getVendorId();
        } else {
            receiverId = order.getCustomerId();
        }

        // Validate receiver exists
        if (receiverId == null) {
            throw new BusinessException("Receiver not found for this order");
        }

        // Validate message type constraints
        if (request.getType() == MessageType.IMAGE) {
            if (request.getImageBase64() == null || request.getImageBase64().isEmpty()) {
                throw new BusinessException("Image is required for IMAGE type messages");
            }
            // Validate and process image
            String processedImage = validateAndProcessImage(request.getImageBase64());
            request.setImageBase64(processedImage);

            // Determine MIME type if not provided
            if (request.getImageMimeType() == null) {
                request.setImageMimeType(detectMimeType(request.getImageBase64()));
            }
        }

        if (request.getType() == MessageType.TEXT && (request.getMessage() == null || request.getMessage().isEmpty())) {
            throw new BusinessException("Message is required for TEXT type messages");
        }

        // Build message
        Message message = Message.builder()
                .orderId(request.getOrderId())
                .senderId(senderId)
                .receiverId(receiverId)
                .senderRole(order.getCustomerId().equals(senderId) ? UserRole.CUSTOMER : UserRole.VENDOR)
                .message(request.getMessage())
                .type(request.getType() != null ? request.getType() : MessageType.TEXT)
                .isRead(false)
                .imageBase64(request.getImageBase64())
                .imageMimeType(request.getImageMimeType())
                .locationLat(request.getLocationLat())
                .locationLng(request.getLocationLng())
                .build();

        // Calculate image size if present
        if (request.getImageBase64() != null) {
            String base64Data = request.getImageBase64().contains(",") ?
                    request.getImageBase64().substring(request.getImageBase64().indexOf(",") + 1) :
                    request.getImageBase64();
            message.setImageSize((long) Base64.getDecoder().decode(base64Data).length);
        }

        Message savedMessage = messageRepository.save(message);

        log.info("💬 Message sent: {} -> {} for order: {} (Type: {})",
                senderId, receiverId, request.getOrderId(), request.getType());

        // Send real-time notification via WebSocket
        sendRealTimeNotification(savedMessage);

        return toDto(savedMessage);
    }

    /**
     * Detect MIME type from base64 image
     */
    private String detectMimeType(String imageBase64) {
        String base64Data = imageBase64.contains(",") ?
                imageBase64.substring(imageBase64.indexOf(",") + 1) :
                imageBase64;

        byte[] bytes = Base64.getDecoder().decode(base64Data);

        if (bytes.length < 4) return "image/jpeg";

        // JPEG
        if (bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xD8 && bytes[2] == (byte) 0xFF) {
            return "image/jpeg";
        }
        // PNG
        if (bytes[0] == (byte) 0x89 && bytes[1] == (byte) 0x50 &&
                bytes[2] == (byte) 0x4E && bytes[3] == (byte) 0x47) {
            return "image/png";
        }
        // GIF
        if (bytes[0] == (byte) 0x47 && bytes[1] == (byte) 0x49 &&
                bytes[2] == (byte) 0x46 && bytes[3] == (byte) 0x38) {
            return "image/gif";
        }
        // WebP
        if (bytes[0] == (byte) 0x52 && bytes[1] == (byte) 0x49 &&
                bytes[2] == (byte) 0x46 && bytes[3] == (byte) 0x46) {
            return "image/webp";
        }

        return "image/jpeg"; // Default
    }

    /**
     * Get messages for an order
     */
    public List<MessageDto> getMessages(UUID userId, UUID orderId) throws BusinessException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getCustomerId().equals(userId) && !order.getVendorId().equals(userId)) {
            throw new BusinessException("You don't have permission to view messages for this order");
        }

        return messageRepository.findByOrderIdOrderByCreatedAtAsc(orderId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get messages with pagination
     */
    public Page<MessageDto> getMessagesPaginated(UUID userId, UUID orderId, Pageable pageable) throws BusinessException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getCustomerId().equals(userId) && !order.getVendorId().equals(userId)) {
            throw new BusinessException("You don't have permission to view messages for this order");
        }

        return messageRepository.findByOrderId(orderId, pageable)
                .map(this::toDto);
    }

    /**
     * Mark all messages as read
     */
    @Transactional
    public void markMessagesAsRead(UUID userId, UUID orderId) throws BusinessException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getCustomerId().equals(userId) && !order.getVendorId().equals(userId)) {
            throw new BusinessException("You don't have permission to update messages for this order");
        }

        messageRepository.markAllAsRead(orderId, userId);
        log.info("✅ Messages marked as read for user: {} in order: {}", userId, orderId);
    }

    /**
     * Get unread message count
     */
    public long getUnreadCount(UUID userId) {
        return messageRepository.countUnreadMessages(userId);
    }

    /**
     * Get unread count for a specific order
     */
    public long getUnreadCountForOrder(UUID userId, UUID orderId) {
        return messageRepository.countUnreadMessagesByOrder(orderId, userId);
    }

    /**
     * Send real-time notification via WebSocket
     */
    private void sendRealTimeNotification(Message message) {
        try {
            messagingTemplate.convertAndSendToUser(
                    message.getReceiverId().toString(),
                    "/queue/messages",
                    toDto(message)
            );

            messagingTemplate.convertAndSend(
                    "/topic/orders/" + message.getOrderId() + "/messages",
                    toDto(message)
            );
        } catch (Exception e) {
            log.error("Failed to send real-time notification: {}", e.getMessage());
        }
    }

    /**
     * Convert to DTO
     */
    private MessageDto toDto(Message message) {
        return MessageDto.builder()
                .id(message.getId())
                .orderId(message.getOrderId())
                .senderId(message.getSenderId())
                .receiverId(message.getReceiverId())
                .senderRole(message.getSenderRole())
                .message(message.getMessage())
                .type(message.getType())
                .isRead(message.isRead())
                .readAt(message.getReadAt())
                .imageBase64(message.getImageBase64())
                .imageMimeType(message.getImageMimeType())
                .imageSize(message.getImageSize())
                .locationLat(message.getLocationLat())
                .locationLng(message.getLocationLng())
                .createdAt(message.getCreatedAt())
                .build();
    }
}