package com.gaslink.api.modules.notification.dto;

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
public class NotificationDto {
    private UUID id;
    private String type;
    private String title;
    private String body;
    private boolean isRead;
    private Instant readAt;
    private String deepLink;
    private String actionData;
    private String imageUrl;
    private Instant createdAt;
}



