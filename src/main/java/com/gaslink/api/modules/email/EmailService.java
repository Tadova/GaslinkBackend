package com.gaslink.api.modules.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.from:gaslink295@gmail.com}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    /**
     * Generic method to send email with HTML content
     */
    public void sendEmail(String userEmail, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(userEmail);
            helper.setSubject(subject);
            helper.setText(content, true); // true = HTML content

            mailSender.send(message);
            log.info("✅ Email sent to: {} with subject: {}", userEmail, subject);

        } catch (MessagingException e) {
            log.error("❌ Failed to send email to: {} with subject: {}", userEmail, subject, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    public void sendPasswordResetEmail(String to, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Reset Your Password - GasLink");

            String resetLink = frontendUrl + "/reset-password?token=" + token;

            Context context = new Context();
            context.setVariable("name", to.split("@")[0]);
            context.setVariable("resetLink", resetLink);
            context.setVariable("expiryMinutes", 30);
            context.setVariable("year", java.time.Year.now().getValue());

            String htmlContent = templateEngine.process("password-reset-email", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Password reset email sent to: {}", to);

        } catch (MessagingException e) {
            log.error("Failed to send password reset email to: {}", to, e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    public void sendPasswordChangedNotification(String to) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Password Changed Successfully - GasLink");

            Context context = new Context();
            context.setVariable("name", to.split("@")[0]);
            context.setVariable("changedAt", java.time.LocalDateTime.now());
            context.setVariable("year", java.time.Year.now().getValue());

            String htmlContent = templateEngine.process("password-changed-email", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Password changed notification sent to: {}", to);

        } catch (MessagingException e) {
            log.error("Failed to send password changed notification to: {}", to, e);
        }
    }

    /**
     * Send welcome email to Super Admin with password setup link
     */
    public void sendSuperAdminWelcomeEmail(String to, String firstName, String resetToken) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("🎉 Welcome as Super Admin - GasLink");

            // Build password setup link
            String setupLink = frontendUrl + "/setup-password?token=" + resetToken;

            Context context = new Context();
            context.setVariable("firstName", firstName);
            context.setVariable("email", to);
            context.setVariable("setupLink", setupLink);
            context.setVariable("expiryMinutes", 30);
            context.setVariable("year", java.time.Year.now().getValue());

            String htmlContent = templateEngine.process("super-admin-welcome-email", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("✅ Super Admin welcome email sent to: {}", to);

        } catch (MessagingException e) {
            log.error("❌ Failed to send Super Admin welcome email to: {}", to, e);
            throw new RuntimeException("Failed to send Super Admin welcome email", e);
        }
    }

    public void sendNewVendorNotificationToAdmins(String adminEmail, String vendorName, String businessName, String vendorEmail) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(adminEmail);
            helper.setSubject("🆕 New Vendor Registration - " + businessName);

            Context context = new Context();
            context.setVariable("adminName", adminEmail.split("@")[0]);
            context.setVariable("vendorName", vendorName);
            context.setVariable("businessName", businessName);
            context.setVariable("vendorEmail", vendorEmail);
            context.setVariable("adminUrl", frontendUrl + "/admin/vendors/pending");
            context.setVariable("year", java.time.Year.now().getValue());

            String htmlContent = templateEngine.process("new-vendor-notification", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("✅ New vendor notification sent to admin: {}", adminEmail);

        } catch (MessagingException e) {
            log.error("❌ Failed to send new vendor notification to admin: {}", adminEmail, e);
        }
    }

    /**
     * Send vendor verification result email
     */
    public void sendVendorVerificationEmail(String to, String vendorName, String businessName,
                                            String status, String rejectionReason) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);

            String subject = status.equals("VERIFIED") ?
                    "✅ Vendor Application Approved - GasLink" :
                    "📋 Vendor Application Update - GasLink";

            helper.setSubject(subject);

            Context context = new Context();
            context.setVariable("vendorName", vendorName);
            context.setVariable("businessName", businessName);
            context.setVariable("status", status);
            context.setVariable("rejectionReason", rejectionReason);
            context.setVariable("vendorDashboardUrl", frontendUrl + "/vendor/dashboard");
            context.setVariable("year", java.time.Year.now().getValue());

            String htmlContent = templateEngine.process("vendor-verification-email", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("✅ Vendor verification email sent to: {}", to);

        } catch (MessagingException e) {
            log.error("❌ Failed to send vendor verification email to: {}", to, e);
        }
    }

    /**
     * Send welcome email to vendor after verification
     */
    public void sendVendorWelcomeEmail(String to, String vendorName, String businessName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("🎉 Welcome to GasLink as a Vendor!");

            Context context = new Context();
            context.setVariable("vendorName", vendorName);
            context.setVariable("businessName", businessName);
            context.setVariable("dashboardUrl", frontendUrl + "/vendor/dashboard");
            context.setVariable("year", java.time.Year.now().getValue());

            String htmlContent = templateEngine.process("vendor-welcome-email", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("✅ Vendor welcome email sent to: {}", to);

        } catch (MessagingException e) {
            log.error("❌ Failed to send vendor welcome email to: {}", to, e);
        }
    }

    /**
     * Send welcome email to new admin with password setup link
     */
    public void sendAdminWelcomeEmail(String to, String fullName, String resetToken, String tempPassword) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("👑 Welcome as Admin - GasLink");

            // Build password setup link
            String setupLink = frontendUrl + "/setup-password?token=" + resetToken;

            Context context = new Context();
            context.setVariable("fullName", fullName);
            context.setVariable("email", to);
            context.setVariable("setupLink", setupLink);
            context.setVariable("tempPassword", tempPassword);
            context.setVariable("expiryMinutes", 30);
            context.setVariable("year", java.time.Year.now().getValue());

            String htmlContent = templateEngine.process("admin-welcome-email", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("✅ Admin welcome email sent to: {}", to);

        } catch (MessagingException e) {
            log.error("❌ Failed to send Admin welcome email to: {}", to, e);
            throw new RuntimeException("Failed to send Admin welcome email", e);
        }
    }
}