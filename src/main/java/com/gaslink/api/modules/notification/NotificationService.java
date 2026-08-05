package com.gaslink.api.modules.notification;

import com.gaslink.api.exception.BusinessException;
import com.gaslink.api.modules.notification.dto.MarkNotificationsReadRequest;
import com.gaslink.api.modules.notification.dto.NotificationCountDto;
import com.gaslink.api.modules.notification.dto.NotificationDto;
import com.gaslink.api.modules.notification.dto.SendNotificationRequest;
import com.gaslink.api.modules.user.User;
import com.gaslink.api.modules.user.UserRepository;
import com.gaslink.api.shared.enums.UserRole;
import com.gaslink.api.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final PushNotificationService pushNotificationService;

    // ========== SEND NOTIFICATIONS ==========

    /**
     * Send notification to a single user
     */
    @Transactional
    public NotificationDto sendNotification(SendNotificationRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Notification notification = Notification.builder()
                .userId(user.getId())
                .type(request.getType())
                .title(request.getTitle())
                .body(request.getBody())
                .isRead(false)
                .deepLink(request.getDeepLink())
                .actionData(request.getActionData())
                .imageUrl(request.getImageUrl())
                .build();

        Notification saved = notificationRepository.save(notification);

        log.info("📨 Notification sent to user: {} - {}", user.getId(), request.getTitle());

        // Send push notification if user has a device token
        if (user.getPushToken() != null && !user.getPushToken().isEmpty()) {
            pushNotificationService.sendPushNotification(
                    user.getPushToken(),
                    request.getTitle(),
                    request.getBody(),
                    Map.of(
                            "type", request.getType(),
                            "notificationId", saved.getId().toString(),
                            "deepLink", request.getDeepLink() != null ? request.getDeepLink() : ""
                    )
            );
        }

        return toDto(saved);
    }

    /**
     * Send notification to multiple users
     */
    @Transactional
    public List<NotificationDto> sendBulkNotifications(List<SendNotificationRequest> requests) {
        return requests.stream()
                .map(this::sendNotification)
                .collect(Collectors.toList());
    }

    /**
     * Send notification to all users with a specific role
     */
    @Transactional
    public void sendNotificationToRole(UserRole role, String title, String body, String type) {
        List<User> users = userRepository.findByRole(role);

        for (User user : users) {
            SendNotificationRequest request = SendNotificationRequest.builder()
                    .userId(user.getId())
                    .type(type)
                    .title(title)
                    .body(body)
                    .build();
            sendNotification(request);
        }

        log.info("📨 Notification sent to {} users with role: {}", users.size(), role);
    }

    /**
     * Send notification to all users
     */
    @Transactional
    public void sendNotificationToAll(String title, String body, String type) {
        List<User> users = userRepository.findAll();

        for (User user : users) {
            SendNotificationRequest request = SendNotificationRequest.builder()
                    .userId(user.getId())
                    .type(type)
                    .title(title)
                    .body(body)
                    .build();
            sendNotification(request);
        }

        log.info("📨 Notification sent to all {} users", users.size());
    }

    // ========== GET NOTIFICATIONS ==========

    /**
     * Get all notifications for a user
     */
    public List<NotificationDto> getNotifications(UUID userId) {
        validateUser(userId);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get notifications for a user with pagination
     */
    public Page<NotificationDto> getNotificationsPaginated(UUID userId, Pageable pageable) {
        validateUser(userId);
        return notificationRepository.findByUserId(userId, pageable)
                .map(this::toDto);
    }

    /**
     * Get unread notifications for a user
     */
    public List<NotificationDto> getUnreadNotifications(UUID userId) {
        validateUser(userId);
        return notificationRepository.findUnreadByUserId(userId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get notification count for a user
     */
    public NotificationCountDto getNotificationCount(UUID userId) {
        validateUser(userId);
        long total = notificationRepository.countByUserId(userId);
        long unread = notificationRepository.countUnread(userId);

        return NotificationCountDto.builder()
                .total(total)
                .unread(unread)
                .build();
    }

    /**
     * Get recent notifications (last 24 hours)
     */
    public List<NotificationDto> getRecentNotifications(UUID userId) {
        validateUser(userId);
        Instant since = Instant.now().minus(24, ChronoUnit.HOURS);
        return notificationRepository.findRecentNotifications(userId, since)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // ========== MARK NOTIFICATIONS ==========

    /**
     * Mark a single notification as read
     */
    @Transactional
    public void markAsRead(UUID userId, UUID notificationId) throws BusinessException {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException("You don't have permission to access this notification");
        }

        notification.setRead(true);
        notification.setReadAt(Instant.now());
        notificationRepository.save(notification);

        log.info("✅ Notification marked as read: {}", notificationId);
    }

    /**
     * Mark multiple notifications as read
     */
    @Transactional
    public void markMultipleAsRead(UUID userId, MarkNotificationsReadRequest request) throws BusinessException {
        validateUser(userId);

        if (request.getNotificationIds() == null || request.getNotificationIds().isEmpty()) {
            throw new BusinessException("No notification IDs provided");
        }

        int updated = notificationRepository.markAsRead(request.getNotificationIds(), userId);
        log.info("✅ {} notifications marked as read", updated);
    }

    /**
     * Mark all notifications as read for a user
     */
    @Transactional
    public void markAllAsRead(UUID userId) {
        validateUser(userId);
        notificationRepository.markAllAsRead(userId);
        log.info("✅ All notifications marked as read for user: {}", userId);
    }

    // ========== DELETE NOTIFICATIONS ==========

    /**
     * Delete a notification
     */
    @Transactional
    public void deleteNotification(UUID userId, UUID notificationId) throws BusinessException {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException("You don't have permission to delete this notification");
        }

        notificationRepository.delete(notification);
        log.info("🗑️ Notification deleted: {}", notificationId);
    }

    /**
     * Delete all read notifications older than 30 days
     */
    @Transactional
    public void cleanOldNotifications() {
        Instant olderThan = Instant.now().minus(30, ChronoUnit.DAYS);
        List<User> users = userRepository.findAll();

        for (User user : users) {
            notificationRepository.deleteOldReadNotifications(user.getId(), olderThan);
        }

        log.info("🧹 Cleaned old notifications");
    }

    // ========== HELPER METHODS ==========

    private void validateUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }
    }

    private NotificationDto toDto(Notification notification) {
        return NotificationDto.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .body(notification.getBody())
                .isRead(notification.isRead())
                .readAt(notification.getReadAt())
                .deepLink(notification.getDeepLink())
                .actionData(notification.getActionData())
                .imageUrl(notification.getImageUrl())
                .createdAt(notification.getCreatedAt())
                .build();
    }


    // Add this method to your NotificationService

    /**
     * Send notification to a specific user with data payload
     */
    public void sendNotificationToUser(UUID userId, String title, String body, Map<String, String> data) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Save in-app notification
        Notification notification = Notification.builder()
                .userId(userId)
                .type("CALL")
                .title(title)
                .body(body)
                .isRead(false)
                .actionData(data != null ? data.toString() : null)
                .build();
        notificationRepository.save(notification);

        // Send push notification if device token exists
        if (user.getPushToken() != null && !user.getPushToken().isEmpty()) {
            pushNotificationService.sendPushNotification(
                    user.getPushToken(),
                    title,
                    body,
                    data
            );
        }

        log.info("📨 Notification sent to user: {}", userId);
    }

    public void notify(UUID vendorId, String subscription, String title, String body) {
    }
}