package com.gaslink.api.config.setup;

import com.gaslink.api.modules.auth.entity.PasswordResetToken;
import com.gaslink.api.modules.auth.repository.PasswordResetTokenRepository;
import com.gaslink.api.modules.email.EmailService;
import com.gaslink.api.modules.user.User;
import com.gaslink.api.modules.user.UserRepository;
import com.gaslink.api.shared.enums.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final PasswordResetTokenRepository resetTokenRepository;

    @Value("${app.super-admin.email:kingzornaments@gmail.com}")
    private String superAdminEmail;

    @Value("${app.super-admin.first-name:Super}")
    private String superAdminFirstName;

    @Value("${app.super-admin.last-name:Admin}")
    private String superAdminLastName;

    @Value("${app.reset-token.expiry-minutes:30}")
    private int resetTokenExpiryMinutes;

    @Override
    public void run(String... args) {
        try {
            log.info("🚀 Starting DataInitializer...");
            createSuperAdminIfNotExists();
            logInitializationStatus();
        } catch (Exception e) {
            log.error("❌ Error during data initialization: {}", e.getMessage(), e);
            e.printStackTrace();
        }
    }

    @Transactional
    public void createSuperAdminIfNotExists() {
        try {
            log.info("📧 Checking if Super Admin exists with email: {}", superAdminEmail);

            // Check if Super Admin exists
            Optional<User> existingUser = userRepository.findByEmail(superAdminEmail);

            if (existingUser.isPresent()) {
                log.info("✅ Super Admin already exists: {}", superAdminEmail);
                log.info("   - ID: {}", existingUser.get().getId());
                log.info("   - Active: {}", existingUser.get().isActive());

                // Check if reset token exists
                createResetTokenIfNotExists(superAdminEmail);
                return;
            }

            log.info("🆕 No Super Admin found. Creating new Super Admin...");

            // Generate a temporary password
            String tempPassword = generateRandomPassword();
            log.info("🔑 Temporary password generated: {}", tempPassword);

            // Create Super Admin - let the auditing handle createdAt/updatedAt
            User superAdmin = User.builder()
                    .fullName(superAdminFirstName + " " + superAdminLastName)
                    .email(superAdminEmail)
                    .passwordHash(passwordEncoder.encode(tempPassword))
                    .phone("+2348131817432")
                    .role(UserRole.SUPER_ADMIN)
                    .isActive(true)
                    .build();

            log.info("💾 Saving Super Admin to database...");
            User savedUser = userRepository.save(superAdmin);
            log.info("✅ Super Admin saved successfully!");
            log.info("   - ID: {}", savedUser.getId());
            log.info("   - Email: {}", savedUser.getEmail());
            log.info("   - Created At: {}", savedUser.getCreatedAt());
            log.info("   - Updated At: {}", savedUser.getUpdatedAt());

            // Create reset token
            String resetToken = UUID.randomUUID().toString();
            LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(resetTokenExpiryMinutes);

            PasswordResetToken token = PasswordResetToken.builder()
                    .token(resetToken)
                    .email(superAdminEmail)
                    .expiryDate(expiryDate)
                    .used(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            log.info("💾 Saving reset token to database...");
            resetTokenRepository.save(token);
            log.info("✅ Reset token saved successfully");

            // Send welcome email
            sendWelcomeEmail(superAdminEmail, superAdminFirstName, resetToken);

            // Log instructions
            logSuperAdminInstructions(superAdminEmail, tempPassword, resetToken);

        } catch (Exception e) {
            log.error("❌ Error creating Super Admin: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void createResetTokenIfNotExists(String email) {
        try {
            // Check if token exists
            Optional<PasswordResetToken> existingToken = resetTokenRepository.findByEmailAndUsedFalse(email);

            if (existingToken.isEmpty()) {
                String resetToken = UUID.randomUUID().toString();
                LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(resetTokenExpiryMinutes);

                PasswordResetToken token = PasswordResetToken.builder()
                        .token(resetToken)
                        .email(email)
                        .expiryDate(expiryDate)
                        .used(false)
                        .createdAt(LocalDateTime.now())
                        .build();

                resetTokenRepository.save(token);
                log.info("✅ New reset token created for existing Super Admin");

                // Send welcome email with new token
                sendWelcomeEmail(email, superAdminFirstName, resetToken);
            } else {
                log.info("✅ Reset token already exists for Super Admin");
                log.info("   - Token: {}", existingToken.get().getToken());
                log.info("   - Expires: {}", existingToken.get().getExpiryDate());
            }

        } catch (Exception e) {
            log.error("Failed to create/check reset token: {}", e.getMessage());
        }
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            password.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        return password.toString();
    }

    private void sendWelcomeEmail(String email, String firstName, String resetToken) {
        try {
            log.info("📧 Sending welcome email to: {}", email);
            emailService.sendSuperAdminWelcomeEmail(email, firstName, resetToken);
            log.info("✅ Welcome email sent successfully!");
        } catch (Exception e) {
            log.error("❌ Failed to send welcome email: {}", e.getMessage(), e);
        }
    }

    private void logSuperAdminInstructions(String email, String tempPassword, String resetToken) {
        String separator = "=".repeat(60);
        String line = "-".repeat(60);

        String message = "\n\n" + separator + "\n" +
                "🎯 SUPER ADMIN ACCOUNT CREATED SUCCESSFULLY!\n" +
                line + "\n" +
                "📧 Email: " + email + "\n" +
                "🔑 Temporary Password: " + tempPassword + "\n" +
                "🔗 Reset Token: " + resetToken + "\n" +
                line + "\n" +
                "⚠️  INSTRUCTIONS FOR SUPER ADMIN:\n" +
                "   1. Check your email inbox (including spam folder)\n" +
                "   2. Use the temporary password to login\n" +
                "   3. OR use this link to set a new password:\n" +
                "      http://localhost:8080/api/v1/auth/reset-password?token=" + resetToken + "\n" +
                line + "\n" +
                "📝 To test the API:\n" +
                "   POST /api/v1/auth/login\n" +
                "   Body: { \"phoneOrEmail\": \"" + email + "\", \"password\": \"" + tempPassword + "\" }\n" +
                separator + "\n";

        log.info(message);
        System.out.println(message);
    }

    private void logInitializationStatus() {
        try {
            long totalUsers = userRepository.count();
            List<User> superAdmins = userRepository.findByRole(UserRole.SUPER_ADMIN);
            long totalSuperAdmins = superAdmins.size();

            log.info("📊 Database Initialization Status:");
            log.info("   - Total Users: {}", totalUsers);
            log.info("   - Total Super Admins: {}", totalSuperAdmins);

            if (totalSuperAdmins > 0) {
                User superAdmin = superAdmins.get(0);
                log.info("✅ Super Admin Details:");
                log.info("   - ID: {}", superAdmin.getId());
                log.info("   - Email: {}", superAdmin.getEmail());
                log.info("   - Phone: {}", superAdmin.getPhone());
                log.info("   - Active: {}", superAdmin.isActive());
                log.info("   - Role: {}", superAdmin.getRole());
                log.info("   - Created At: {}", superAdmin.getCreatedAt());
                log.info("   - Updated At: {}", superAdmin.getUpdatedAt());
            } else {
                log.warn("⚠️ No Super Admin found in the database!");
            }
        } catch (Exception e) {
            log.warn("Could not log initialization status: {}", e.getMessage());
        }
    }
}