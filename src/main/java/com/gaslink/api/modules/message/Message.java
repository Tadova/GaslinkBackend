package com.gaslink.api.modules.message;

import com.gaslink.api.shared.audit.AuditableEntity;
import com.gaslink.api.shared.enums.MessageType;
import com.gaslink.api.shared.enums.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "sender_id", nullable = false)
    private UUID senderId;

    @Column(name = "receiver_id", nullable = false)
    private UUID receiverId;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender_role", nullable = false)
    private UserRole senderRole; // CUSTOMER or VENDOR

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private MessageType type = MessageType.TEXT;

    @Column(name = "is_read")
    private boolean isRead = false;

    @Column(name = "read_at")
    private Instant readAt;

    @Column(name = "image_base64", columnDefinition = "TEXT")
    private String imageBase64; // Base64 encoded image

    @Column(name = "image_mime_type")
    private String imageMimeType; // image/jpeg, image/png, etc.

    @Column(name = "image_size")
    private Long imageSize; // Size in bytes

    @Column(name = "location_lat")
    private Double locationLat;

    @Column(name = "location_lng")
    private Double locationLng;
}