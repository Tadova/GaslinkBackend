package com.gaslink.api.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Slf4j
@Configuration
public class S3Config {

    @Value("${app.s3.endpoint:https://s3.amazonaws.com}")
    private String endpoint;

    @Value("${app.s3.access-key:}")
    private String accessKey;

    @Value("${app.s3.secret-key:}")
    private String secretKey;

    @Value("${app.s3.region:eu-west-2}")
    private String region;

    @Value("${app.s3.bucket:gaslink-bucket}")
    private String bucket;

    @Bean
    public S3Client s3Client() {
        // Check if credentials are provided
        if (accessKey == null || accessKey.isEmpty() || secretKey == null || secretKey.isEmpty()) {
            log.warn("⚠️ S3 credentials not configured. Returning null S3Client.");
            log.warn("   To enable S3, set app.s3.access-key and app.s3.secret-key");
            return null;
        }

        try {
            log.info("🔐 Initializing S3 client...");
            log.info("   - Endpoint: {}", endpoint);
            log.info("   - Region: {}", region);
            log.info("   - Bucket: {}", bucket);

            S3Client client = S3Client.builder()
                    .endpointOverride(URI.create(endpoint))
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(accessKey, secretKey)))
                    .build();

            log.info("✅ S3 client initialized successfully!");
            return client;

        } catch (Exception e) {
            log.error("❌ Failed to initialize S3 client: {}", e.getMessage());
            return null;
        }
    }

    @Bean
    public String s3Bucket() {
        return bucket;
    }

    @Bean
    public boolean s3Enabled() {
        return accessKey != null && !accessKey.isEmpty() &&
                secretKey != null && !secretKey.isEmpty();
    }
}