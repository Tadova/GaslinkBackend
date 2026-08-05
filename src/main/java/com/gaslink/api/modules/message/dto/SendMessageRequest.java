package com.gaslink.api.modules.message.dto;

import com.gaslink.api.shared.enums.MessageType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to send a message")
public class SendMessageRequest {

    @NotNull(message = "Order ID is required")
    @Schema(description = "Order ID", required = true)
    private UUID orderId;

    @NotNull(message = "Receiver ID is required")
    @Schema(description = "Receiver ID (vendor or customer)", required = true)
    private UUID receiverId;

    @Schema(description = "Message content", example = "I'm on my way")
    private String message;

    @Schema(description = "Message type", example = "TEXT")
    private MessageType type = MessageType.TEXT;

    @Schema(description = "Image in base64 format (max 1MB)",
            example = "data:image/jpeg;base64,/9j/4AAQSkZJRg...")
    private String imageBase64;

    @Schema(description = "Image MIME type", example = "image/jpeg")
    private String imageMimeType;

    @Schema(description = "Location latitude (for location messages)")
    private Double locationLat;

    @Schema(description = "Location longitude (for location messages)")
    private Double locationLng;
}