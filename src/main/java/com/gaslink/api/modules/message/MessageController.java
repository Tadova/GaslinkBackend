package com.gaslink.api.modules.message;

import com.gaslink.api.exception.BusinessException;
import com.gaslink.api.modules.message.dto.MessageDto;
import com.gaslink.api.modules.message.dto.SendMessageRequest;
import com.gaslink.api.modules.message.dto.ShareLocationRequest;
import com.gaslink.api.modules.message.service.MessageService;
import com.gaslink.api.response.ApiResponse;
import com.gaslink.api.shared.enums.MessageType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
@Tag(name = "Messages", description = "In-app messaging between vendors and customers")
@SecurityRequirement(name = "bearerAuth")
public class MessageController {

    private final MessageService messageService;

    // ============================================================
    // 1. POST / - Send Message (Text or Image)
    // ============================================================

    @Operation(
            summary = "Send a message",
            description = "Send a text or image message to the other party. Images must be base64 encoded (max 1MB)."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Message sent successfully",
                    content = @Content(
                            examples = {
                                    @ExampleObject(
                                            name = "Text Message",
                                            value = """
                                    {
                                        "success": true,
                                        "message": "Message sent successfully",
                                        "data": {
                                            "id": "msg_550e8400-e29b-41d4-a716-446655440000",
                                            "orderId": "550e8400-e29b-41d4-a716-446655440000",
                                            "senderId": "550e8400-e29b-41d4-a716-446655440001",
                                            "senderRole": "VENDOR",
                                            "message": "I'm on my way to deliver",
                                            "type": "TEXT",
                                            "isRead": false,
                                            "createdAt": "2026-08-05T14:30:00Z"
                                        },
                                        "timestamp": "2026-08-05T14:30:00Z"
                                    }
                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Image Message",
                                            value = """
                                    {
                                        "success": true,
                                        "message": "Message sent successfully",
                                        "data": {
                                            "id": "msg_550e8400-e29b-41d4-a716-446655440000",
                                            "orderId": "550e8400-e29b-41d4-a716-446655440000",
                                            "senderId": "550e8400-e29b-41d4-a716-446655440001",
                                            "senderRole": "VENDOR",
                                            "message": "Here is the gas cylinder",
                                            "type": "IMAGE",
                                            "imageBase64": "data:image/jpeg;base64,/9j/4AAQSkZJRg...",
                                            "imageMimeType": "image/jpeg",
                                            "imageSize": 245678,
                                            "isRead": false,
                                            "createdAt": "2026-08-05T14:30:00Z"
                                        },
                                        "timestamp": "2026-08-05T14:30:00Z"
                                    }
                                    """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid image format or size > 1MB"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MessageDto>> sendMessage(
            @Parameter(hidden = true) Authentication auth,
            @Valid @RequestBody SendMessageRequest request) throws BusinessException {
        UUID senderId = UUID.fromString(auth.getName());
        log.info("💬 User {} sending {} message for order: {}",
                senderId, request.getType(), request.getOrderId());
        return ResponseEntity.ok(ApiResponse.ok(
                "Message sent successfully",
                messageService.sendMessage(senderId, request)
        ));
    }

    // ============================================================
    // 2. GET /order/{orderId} - Get Messages
    // ============================================================

    @Operation(
            summary = "Get all messages for an order",
            description = "Retrieves all messages between vendor and customer for a specific order"
    )
    @GetMapping("/order/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<MessageDto>>> getMessages(
            @Parameter(hidden = true) Authentication auth,
            @Parameter(description = "Order ID", required = true) @PathVariable UUID orderId) throws BusinessException {
        UUID userId = UUID.fromString(auth.getName());
        log.info("📨 User {} fetching messages for order: {}", userId, orderId);
        return ResponseEntity.ok(ApiResponse.ok(
                "Messages retrieved successfully",
                messageService.getMessages(userId, orderId)
        ));
    }

    // ============================================================
    // 3. GET /order/{orderId}/paginated - Get Messages with Pagination
    // ============================================================

    @Operation(
            summary = "Get messages with pagination",
            description = "Retrieves messages with pagination support for large conversations"
    )
    @GetMapping("/order/{orderId}/paginated")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<MessageDto>>> getMessagesPaginated(
            @Parameter(hidden = true) Authentication auth,
            @Parameter(description = "Order ID", required = true) @PathVariable UUID orderId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) throws BusinessException {
        UUID userId = UUID.fromString(auth.getName());
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.ok(
                messageService.getMessagesPaginated(userId, orderId, pageable)
        ));
    }

    // ============================================================
    // 4. PUT /read/{orderId} - Mark All Messages as Read
    // ============================================================

    @Operation(
            summary = "Mark all messages as read",
            description = "Marks all unread messages in a conversation as read"
    )
    @PutMapping("/read/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> markMessagesAsRead(
            @Parameter(hidden = true) Authentication auth,
            @Parameter(description = "Order ID", required = true) @PathVariable UUID orderId) throws BusinessException {
        UUID userId = UUID.fromString(auth.getName());
        log.info("✅ User {} marking messages as read for order: {}", userId, orderId);
        messageService.markMessagesAsRead(userId, orderId);
        return ResponseEntity.ok(ApiResponse.ok("Messages marked as read", null));
    }

    // ============================================================
    // 5. GET /unread/count - Get Unread Message Count
    // ============================================================

    @Operation(
            summary = "Get total unread message count",
            description = "Returns the total number of unread messages across all orders"
    )
    @GetMapping("/unread/count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @Parameter(hidden = true) Authentication auth) {
        UUID userId = UUID.fromString(auth.getName());
        long count = messageService.getUnreadCount(userId);
        return ResponseEntity.ok(ApiResponse.ok(count));
    }

    // ============================================================
    // 6. GET /unread/order/{orderId} - Get Unread Count for Order
    // ============================================================

    @Operation(
            summary = "Get unread count for a specific order",
            description = "Returns the number of unread messages in a specific order conversation"
    )
    @GetMapping("/unread/order/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Long>> getUnreadCountForOrder(
            @Parameter(hidden = true) Authentication auth,
            @Parameter(description = "Order ID", required = true) @PathVariable UUID orderId) throws BusinessException {
        UUID userId = UUID.fromString(auth.getName());
        long count = messageService.getUnreadCountForOrder(userId, orderId);
        return ResponseEntity.ok(ApiResponse.ok(count));
    }

    // ============================================================
    // 7. POST /share-location - Share Location
    // ============================================================

    @Operation(
            summary = "Share location",
            description = "Share real-time location with the other party"
    )
    @PostMapping("/share-location")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MessageDto>> shareLocation(
            @Parameter(hidden = true) Authentication auth,
            @Valid @RequestBody ShareLocationRequest request) throws BusinessException {
        UUID senderId = UUID.fromString(auth.getName());
        log.info("📍 User {} sharing location for order: {}", senderId, request.getOrderId());

        SendMessageRequest messageRequest = SendMessageRequest.builder()
                .orderId(request.getOrderId())
                .receiverId(request.getReceiverId())
                .message("📍 " + request.getMessage())
                .type(MessageType.LOCATION)
                .locationLat(request.getLat())
                .locationLng(request.getLng())
                .build();

        return ResponseEntity.ok(ApiResponse.ok(
                "Location shared successfully",
                messageService.sendMessage(senderId, messageRequest)
        ));
    }
}