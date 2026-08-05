package com.gaslink.api.modules.otp;

import com.gaslink.api.exception.BusinessException;
import com.gaslink.api.modules.email.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final RedisTemplate<String, String> redisTemplate;
    private final EmailService emailService;

    @Value("${app.otp.expiry-minutes:5}")
    private int otpExpiryMinutes;

    private static final String OTP_PREFIX = "otp:";
    private static final int OTP_LENGTH = 6;
    private static final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generate a 6-digit OTP
     */
    public String generateOtp(String email) {
        // Generate 6-digit OTP
        int otp = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(otp);
    }

    /**
     * Generate OTP and store in Redis
     */
    public String generateAndStoreOtp(String email) {
        String otp = generateOtp(email);
        storeOtp(email, otp);
        return otp;
    }

    /**
     * Store OTP in Redis with expiry
     */
    public void storeOtp(String email, String otp) {
        String key = OTP_PREFIX + email;
        redisTemplate.opsForValue().set(key, otp, otpExpiryMinutes, TimeUnit.MINUTES);
        log.info("✅ OTP stored for: {} (expires in {} minutes)", email, otpExpiryMinutes);
    }

    /**
     * Verify OTP
     */
    public boolean verifyOtp(String email, String otp) {
        if (email == null || otp == null) {
            return false;
        }

        String key = OTP_PREFIX + email;
        String storedOtp = redisTemplate.opsForValue().get(key);

        if (storedOtp == null) {
            log.warn("⚠️ OTP not found or expired for: {}", email);
            return false;
        }

        boolean isValid = storedOtp.equals(otp);
        if (isValid) {
            // Delete OTP after successful verification
            redisTemplate.delete(key);
            log.info("✅ OTP verified successfully for: {}", email);
        } else {
            log.warn("⚠️ Invalid OTP attempt for: {}", email);
        }

        return isValid;
    }

    /**
     * Verify OTP and throw exception if invalid
     */
    public void verifyOtpOrThrow(String email, String otp) throws BusinessException {
        if (!verifyOtp(email, otp)) {
            throw new BusinessException("Invalid or expired OTP. Please request a new one.");
        }
    }

    /**
     * Send OTP via email
     */
    public void sendOtpEmail(String email, String otp) throws BusinessException {
        try {
            String subject = "🔐 Your GasLink OTP Code";
            String content = String.format("""
                <html>
                <body>
                    <h2>Your OTP Code</h2>
                    <p>Hello,</p>
                    <p>Your verification code is:</p>
                    <h1 style="font-size: 32px; color: #0d9488; letter-spacing: 4px;">%s</h1>
                    <p>This code will expire in <strong>%d minutes</strong>.</p>
                    <p>If you didn't request this, please ignore this email.</p>
                    <p>Best regards,<br/>GasLink Team</p>
                </body>
                </html>
                """, otp, otpExpiryMinutes);

            emailService.sendEmail(email, subject, content);
            log.info("📧 OTP email sent to: {}", email);
        } catch (Exception e) {
            log.error("❌ Failed to send OTP email to: {}", email, e);
            throw new BusinessException("Failed to send OTP. Please try again.");
        }
    }

    /**
     * Send OTP via SMS (placeholder - implement with SMS service)
     */
    public void sendOtpSms(String phone, String otp) {
        // TODO: Implement SMS sending (Twilio, Vonage, etc.)
        log.info("📱 OTP SMS sent to: {} (OTP: {})", phone, otp);
        // For now, just log it
        System.out.println("SMS OTP for " + phone + " => " + otp);
    }

    /**
     * Resend OTP
     */
    public void resendOtp(String email) throws BusinessException {
        // Delete existing OTP
        String key = OTP_PREFIX + email;
        redisTemplate.delete(key);

        // Generate and send new OTP
        String otp = generateAndStoreOtp(email);
        sendOtpEmail(email, otp);
        log.info("🔄 OTP resent to: {}", email);
    }

    /**
     * Validate OTP format
     */
    public boolean isValidOtpFormat(String otp) {
        return otp != null && otp.matches("^\\d{6}$");
    }

    /**
     * Get remaining OTP expiry time in seconds
     */
    public Long getOtpExpiryTime(String email) {
        String key = OTP_PREFIX + email;
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        return ttl != null && ttl > 0 ? ttl : 0L;
    }

    /**
     * Check if OTP exists for email
     */
    public boolean hasOtp(String email) {
        String key = OTP_PREFIX + email;
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }

    /**
     * Clear OTP for email
     */
    public void clearOtp(String email) {
        String key = OTP_PREFIX + email;
        redisTemplate.delete(key);
        log.info("🗑️ OTP cleared for: {}", email);
    }
}