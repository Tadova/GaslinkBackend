package com.gaslink.api.modules.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendNotificationRequest {
    private UUID userId;
    private String type;
    private String title;
    private String body;
    private String deepLink;
    private String actionData;
    private String imageUrl;
}