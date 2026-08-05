package com.gaslink.api.modules.notification;

import com.gaslink.api.shared.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "type", nullable = false)
    private String type; // ORDER, MESSAGE, SUBSCRIPTION, PROMOTION, SYSTEM

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "body", nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "is_read")
    private boolean isRead = false;

    @Column(name = "read_at")
    private Instant readAt;

    @Column(name = "deep_link")
    private String deepLink; // e.g., gaslink://order/123

    @Column(name = "action_data", columnDefinition = "TEXT")
    private String actionData; // JSON data for actions

    @Column(name = "image_url")
    private String imageUrl;
}