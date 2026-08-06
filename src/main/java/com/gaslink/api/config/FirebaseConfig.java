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
import org.springframework.lang.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${firebase.config.path:firebase-service-account.json}")
    private String firebaseConfigValue;

    /**
     * Initialize Firebase App.
     * Accepts EITHER:
     *  - raw JSON content (e.g. set directly as the FIREBASE_CONFIG_PATH / firebase.config.path
     *    env var value on Render), OR
     *  - a classpath file path (for local dev, e.g. firebase-service-account.json in src/main/resources)
     */
    @Bean
    public FirebaseApp firebaseApp() {
        try {
            String trimmed = firebaseConfigValue == null ? "" : firebaseConfigValue.trim();

            InputStream credentialsStream;

            if (trimmed.startsWith("{")) {
                // Raw JSON content passed directly via env var
                log.info("🔐 Initializing Firebase from inline JSON config (env var)");
                credentialsStream = new ByteArrayInputStream(trimmed.getBytes(StandardCharsets.UTF_8));
            } else {
                // Treat as a classpath file path (local dev)
                log.info("🔐 Initializing Firebase from classpath file: {}", trimmed);
                ClassPathResource resource = new ClassPathResource(trimmed);
                if (!resource.exists()) {
                    log.error("❌ Firebase service account file not found on classpath: {}", trimmed);
                    log.error("⚠️ Either place 'firebase-service-account.json' in src/main/resources/, "
                            + "or set firebase.config.path to the raw JSON content via an env var.");
                    return null;
                }
                credentialsStream = resource.getInputStream();
            }

            try (InputStream serviceAccount = credentialsStream) {
                GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(credentials)
                        .build();

                FirebaseApp app = FirebaseApp.initializeApp(options);
                log.info("✅ Firebase initialized successfully!");
                log.info("📱 Firebase App Name: {}", app.getName());
                return app;
            }

        } catch (IOException e) {
            log.error("❌ Failed to initialize Firebase: {}", e.getMessage());
            log.error("⚠️ Please check your Firebase service account JSON / file path");
            return null;
        }
    }

    /**
     * Create FirebaseMessaging bean for sending push notifications.
     * Marked @Nullable so Spring doesn't fail the whole context if Firebase
     * couldn't initialize (e.g. missing/misconfigured credentials) — push
     * notifications will just be disabled instead of crashing the app.
     */
    @Bean
    public FirebaseMessaging firebaseMessaging(@Nullable FirebaseApp firebaseApp) {
        if (firebaseApp == null) {
            log.warn("⚠️ FirebaseApp is null. Push notifications will be disabled.");
            return null;
        }

        FirebaseMessaging messaging = FirebaseMessaging.getInstance(firebaseApp);
        log.info("✅ FirebaseMessaging initialized successfully!");
        return messaging;
    }
}