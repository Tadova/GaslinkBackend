package com.gaslink.api.modules.notification;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class PushNotificationService {

    private final FirebaseMessaging firebaseMessaging;

    public PushNotificationService(FirebaseMessaging firebaseMessaging) {
        this.firebaseMessaging = firebaseMessaging;
    }

    /**
     * Send push notification to a single device
     */
    public void sendPushNotification(String deviceToken, String title, String body) {
        sendPushNotification(deviceToken, title, body, null);
    }

    /**
     * Send push notification with data payload
     */
    public void sendPushNotification(String deviceToken, String title, String body, Map<String, String> data) {
        // Check if Firebase is initialized
        if (firebaseMessaging == null) {
            log.warn("⚠️ FirebaseMessaging is null. Push notification not sent to: {}", deviceToken);
            return;
        }

        // Validate device token
        if (deviceToken == null || deviceToken.isEmpty()) {
            log.warn("⚠️ Device token is null or empty. Cannot send notification.");
            return;
        }

        try {
            Message.Builder messageBuilder = Message.builder()
                    .setToken(deviceToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build());

            // Add data payload if present
            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }

            Message message = messageBuilder.build();
            String response = firebaseMessaging.send(message);
            log.info("✅ Push notification sent successfully: {}", response);

        } catch (Exception e) {
            log.error("❌ Failed to send push notification: {}", e.getMessage());
        }
    }

    /**
     * Send push notification to multiple devices
     */
    public void sendMulticastPushNotification(java.util.List<String> deviceTokens, String title, String body) {
        sendMulticastPushNotification(deviceTokens, title, body, null);
    }

    /**
     * Send push notification to multiple devices with data payload
     */
    public void sendMulticastPushNotification(java.util.List<String> deviceTokens, String title, String body, Map<String, String> data) {
        if (firebaseMessaging == null) {
            log.warn("⚠️ FirebaseMessaging is null. Multicast push notification not sent.");
            return;
        }

        if (deviceTokens == null || deviceTokens.isEmpty()) {
            log.warn("⚠️ No device tokens provided.");
            return;
        }

        try {
            com.google.firebase.messaging.MulticastMessage.Builder messageBuilder =
                    com.google.firebase.messaging.MulticastMessage.builder()
                            .addAllTokens(deviceTokens)
                            .setNotification(Notification.builder()
                                    .setTitle(title)
                                    .setBody(body)
                                    .build());

            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }

            com.google.firebase.messaging.MulticastMessage message = messageBuilder.build();
            com.google.firebase.messaging.BatchResponse response = firebaseMessaging.sendMulticast(message);
            log.info("✅ Multicast push notification sent to {} devices", response.getSuccessCount());

        } catch (Exception e) {
            log.error("❌ Failed to send multicast push notification: {}", e.getMessage());
        }
    }

    /**
     * Send push notification to a topic
     */
    public void sendToTopic(String topic, String title, String body, Map<String, String> data) {
        if (firebaseMessaging == null) {
            log.warn("⚠️ FirebaseMessaging is null. Topic push notification not sent.");
            return;
        }

        try {
            Message.Builder messageBuilder = Message.builder()
                    .setTopic(topic)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build());

            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }

            Message message = messageBuilder.build();
            String response = firebaseMessaging.send(message);
            log.info("✅ Push notification sent to topic '{}': {}", topic, response);

        } catch (Exception e) {
            log.error("❌ Failed to send push notification to topic: {}", e.getMessage());
        }
    }
}