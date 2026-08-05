package com.gaslink.api.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${firebase.config.path:firebase-service-account.json}")
    private String firebaseConfigPath;

    /**
     * Initialize Firebase App
     * This will run on application startup
     */
    @Bean
    public FirebaseApp firebaseApp() {
        try {
            log.info("🔐 Initializing Firebase with config: {}", firebaseConfigPath);

            // Check if file exists
            ClassPathResource resource = new ClassPathResource(firebaseConfigPath);
            if (!resource.exists()) {
                log.error("❌ Firebase service account file not found: {}", firebaseConfigPath);
                log.error("⚠️ Please place 'firebase-service-account.json' in src/main/resources/");
                return null;
            }

            // Load credentials
            try (InputStream serviceAccount = resource.getInputStream()) {
                GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(credentials)
                        .build();

                // Initialize Firebase App
                FirebaseApp app = FirebaseApp.initializeApp(options);
                log.info("✅ Firebase initialized successfully!");
                log.info("📱 Firebase App Name: {}", app.getName());
                return app;
            }

        } catch (IOException e) {
            log.error("❌ Failed to initialize Firebase: {}", e.getMessage());
            log.error("⚠️ Please check your firebase-service-account.json file");
            return null;
        }
    }

    /**
     * Create FirebaseMessaging bean for sending push notifications
     */
    @Bean
    public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        if (firebaseApp == null) {
            log.warn("⚠️ FirebaseApp is null. Push notifications will be disabled.");
            return null;
        }

        FirebaseMessaging messaging = FirebaseMessaging.getInstance(firebaseApp);
        log.info("✅ FirebaseMessaging initialized successfully!");
        return messaging;
    }
}