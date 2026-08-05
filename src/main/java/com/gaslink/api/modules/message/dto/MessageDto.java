package com.gaslink.api.modules.message.dto;

import com.gaslink.api.shared.enums.MessageType;
import com.gaslink.api.shared.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Message response DTO")
public class MessageDto {
    private UUID id;
    private UUID orderId;
    private UUID senderId;
    private UUID receiverId;
    private UserRole senderRole;
    private String message;
    private MessageType type;
    private boolean isRead;
    private Instant readAt;
    private String imageBase64;
    private String imageMimeType;
    private Long imageSize;
    private Double locationLat;
    private Double locationLng;
    private Instant createdAt;
}