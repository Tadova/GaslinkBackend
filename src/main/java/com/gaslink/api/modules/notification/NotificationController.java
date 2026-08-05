package com.gaslink.api.modules.notification;

import com.gaslink.api.exception.BusinessException;
import com.gaslink.api.modules.notification.dto.MarkNotificationsReadRequest;
import com.gaslink.api.modules.notification.dto.NotificationCountDto;
import com.gaslink.api.modules.notification.dto.NotificationDto;
import com.gaslink.api.modules.notification.dto.SendNotificationRequest;
import com.gaslink.api.response.ApiResponse;
import com.gaslink.api.shared.enums.UserRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "Notification management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final NotificationService notificationService;

    // ========== GET NOTIFICATIONS ==========

    @Operation(summary = "Get all notifications for current user")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<NotificationDto>>> getNotifications(
            @Parameter(hidden = true) Authentication auth) {
        UUID userId = UUID.fromString(auth.getName());
        return ResponseEntity.ok(ApiResponse.ok(notificationService.getNotifications(userId)));
    }

    @Operation(summary = "Get notifications with pagination")
    @GetMapping("/paginated")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<NotificationDto>>> getNotificationsPaginated(
            @Parameter(hidden = true) Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        UUID userId = UUID.fromString(auth.getName());
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.ok(notificationService.getNotificationsPaginated(userId, pageable)));
    }

    @Operation(summary = "Get unread notifications")
    @GetMapping("/unread")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<NotificationDto>>> getUnreadNotifications(
            @Parameter(hidden = true) Authentication auth) {
        UUID userId = UUID.fromString(auth.getName());
        return ResponseEntity.ok(ApiResponse.ok(notificationService.getUnreadNotifications(userId)));
    }

    @Operation(summary = "Get notification count")
    @GetMapping("/count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<NotificationCountDto>> getNotificationCount(
            @Parameter(hidden = true) Authentication auth) {
        UUID userId = UUID.fromString(auth.getName());
        return ResponseEntity.ok(ApiResponse.ok(notificationService.getNotificationCount(userId)));
    }

    @Operation(summary = "Get recent notifications (last 24 hours)")
    @GetMapping("/recent")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<NotificationDto>>> getRecentNotifications(
            @Parameter(hidden = true) Authentication auth) {
        UUID userId = UUID.fromString(auth.getName());
        return ResponseEntity.ok(ApiResponse.ok(notificationService.getRecentNotifications(userId)));
    }

    // ========== MARK NOTIFICATIONS ==========

    @Operation(summary = "Mark a notification as read")
    @PutMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @Parameter(hidden = true) Authentication auth,
            @Parameter(description = "Notification ID") @PathVariable UUID id) throws BusinessException {
        UUID userId = UUID.fromString(auth.getName());
        notificationService.markAsRead(userId, id);
        return ResponseEntity.ok(ApiResponse.ok("Notification marked as read", null));
    }

    @Operation(summary = "Mark multiple notifications as read")
    @PutMapping("/read/bulk")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> markMultipleAsRead(
            @Parameter(hidden = true) Authentication auth,
            @Valid @RequestBody MarkNotificationsReadRequest request) throws BusinessException {
        UUID userId = UUID.fromString(auth.getName());
        notificationService.markMultipleAsRead(userId, request);
        return ResponseEntity.ok(ApiResponse.ok("Notifications marked as read", null));
    }

    @Operation(summary = "Mark all notifications as read")
    @PutMapping("/read/all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @Parameter(hidden = true) Authentication auth) {
        UUID userId = UUID.fromString(auth.getName());
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponse.ok("All notifications marked as read", null));
    }

    // ========== DELETE NOTIFICATIONS ==========

    @Operation(summary = "Delete a notification")
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @Parameter(hidden = true) Authentication auth,
            @Parameter(description = "Notification ID") @PathVariable UUID id) throws BusinessException {
        UUID userId = UUID.fromString(auth.getName());
        notificationService.deleteNotification(userId, id);
        return ResponseEntity.ok(ApiResponse.ok("Notification deleted", null));
    }

    // ========== ADMIN ENDPOINTS ==========

    @Operation(summary = "Send notification to a user (Admin only)")
    @PostMapping("/admin/send")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<NotificationDto>> sendNotification(
            @Valid @RequestBody SendNotificationRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(notificationService.sendNotification(request)));
    }

    @Operation(summary = "Send notification to all users with a role (Admin only)")
    @PostMapping("/admin/send/role/{role}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> sendNotificationToRole(
            @Parameter(description = "User role") @PathVariable UserRole role,
            @RequestParam String title,
            @RequestParam String body,
            @RequestParam(defaultValue = "SYSTEM") String type) {
        notificationService.sendNotificationToRole(role, title, body, type);
        return ResponseEntity.ok(ApiResponse.ok("Notifications sent", null));
    }

    @Operation(summary = "Send notification to all users (Admin only)")
    @PostMapping("/admin/send/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> sendNotificationToAll(
            @RequestParam String title,
            @RequestParam String body,
            @RequestParam(defaultValue = "SYSTEM") String type) {
        notificationService.sendNotificationToAll(title, body, type);
        return ResponseEntity.ok(ApiResponse.ok("Notifications sent to all users", null));
    }
}