package com.gaslink.api.modules.subscription;

import com.gaslink.api.exception.BusinessException;
import com.gaslink.api.modules.email.EmailService;
import com.gaslink.api.modules.notification.NotificationService;
import com.gaslink.api.modules.payment.PaystackClient;
import com.gaslink.api.modules.payment.dto.InitiatePaymentResponse;
import com.gaslink.api.modules.subscription.dto.CreateSubscriptionRequest;
import com.gaslink.api.modules.subscription.dto.SubscriptionDto;
import com.gaslink.api.modules.user.User;
import com.gaslink.api.modules.user.UserRepository;
import com.gaslink.api.modules.vendor.Vendor;
import com.gaslink.api.modules.vendor.VendorRepository;
import com.gaslink.api.shared.enums.BillingCycle;
import com.gaslink.api.shared.enums.SubscriptionPlan;
import com.gaslink.api.shared.enums.SubscriptionStatus;
import com.gaslink.api.shared.enums.VendorAccountStatus;
import com.gaslink.api.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final VendorRepository vendorRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final PaystackClient paystackClient;

    // Pricing
    private static final BigDecimal BASIC_PRICE = new BigDecimal("5000.00"); // Monthly
    private static final BigDecimal PREMIUM_PRICE = new BigDecimal("50000.00"); // Annual

    private static final int FREE_TRIAL_DAYS = 30;
    private static final int EXPIRY_REMINDER_DAYS = 5;

    /**
     * Get user email from vendor ID
     */
    private String getVendorEmail(UUID vendorId) {
        return userRepository.findById(vendorId)
                .map(User::getEmail)
                .orElse(null);
    }

    /**
     * Get user full name from vendor ID
     */
    private String getVendorName(UUID vendorId) {
        return userRepository.findById(vendorId)
                .map(User::getFullName)
                .orElse("Vendor");
    }

    /**
     * Get price and billing cycle based on plan
     */
    private SubscriptionDetails getSubscriptionDetails(String plan) {
        if ("PREMIUM".equalsIgnoreCase(plan)) {
            return new SubscriptionDetails(PREMIUM_PRICE, BillingCycle.ANNUAL, 365);
        } else {
            // BASIC plan
            return new SubscriptionDetails(BASIC_PRICE, BillingCycle.MONTHLY, 30);
        }
    }

    /**
     * Inner class for subscription details
     */
    private static class SubscriptionDetails {
        final BigDecimal amount;
        final BillingCycle billingCycle;
        final int days;

        SubscriptionDetails(BigDecimal amount, BillingCycle billingCycle, int days) {
            this.amount = amount;
            this.billingCycle = billingCycle;
            this.days = days;
        }
    }

    /**
     * Activate free trial for newly verified vendor
     */
    @Transactional
    public SubscriptionDto activateFreeTrial(UUID vendorId) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

        // Check if vendor already has a subscription
        if (subscriptionRepository.existsByVendorIdAndStatus(vendorId, SubscriptionStatus.ACTIVE)) {
            log.info("Vendor {} already has an active subscription", vendorId);
            return getMySubscription(vendorId);
        }

        // Deactivate any existing free trial
        subscriptionRepository.findTopByVendorIdOrderByExpiresAtDesc(vendorId)
                .ifPresent(sub -> {
                    sub.setStatus(SubscriptionStatus.EXPIRED);
                    subscriptionRepository.save(sub);
                });

        // Create free trial subscription
        Instant now = Instant.now();
        Instant expiresAt = now.plus(FREE_TRIAL_DAYS, ChronoUnit.DAYS);

        Subscription subscription = Subscription.builder()
                .vendorId(vendorId)
                .plan(SubscriptionPlan.FREE_TRIAL)
                .amount(BigDecimal.ZERO)
                .billingCycle(BillingCycle.MONTHLY)
                .status(SubscriptionStatus.FREE_TRIAL)
                .startedAt(now)
                .expiresAt(expiresAt)
                .build();

        Subscription saved = subscriptionRepository.save(subscription);

        // Update vendor subscription status
        vendor.setSubscriptionStatus("ACTIVE");
        vendor.setAccountStatus(VendorAccountStatus.ENABLED);
        vendorRepository.save(vendor);

        log.info("✅ Free trial activated for vendor: {}", vendorId);

        // Send email notification about free trial
        sendFreeTrialEmail(vendorId, expiresAt);

        // Send push notification
        sendSubscriptionNotification(vendorId, "🎉 Free Trial Activated!",
                "Your 30-day free trial has started! Expires on: " + expiresAt);

        return toDto(saved);
    }

    /**
     * Subscribe to a plan (BASIC = Monthly, PREMIUM = Annual)
     */
    @Transactional
    public InitiatePaymentResponse initiateSubscriptionPayment(UUID vendorId, CreateSubscriptionRequest request) throws BusinessException {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

        // Get user email
        String userEmail = getVendorEmail(vendorId);
        if (userEmail == null) {
            throw new BusinessException("User email not found for this vendor");
        }

        // Validate vendor is verified
        if (vendor.getVerificationStatus() != com.gaslink.api.shared.enums.VerificationStatus.VERIFIED) {
            throw new BusinessException("Vendor must be verified to subscribe");
        }

        // Validate plan
        if (!"BASIC".equalsIgnoreCase(request.getPlan()) && !"PREMIUM".equalsIgnoreCase(request.getPlan())) {
            throw new BusinessException("Invalid plan. Must be 'BASIC' (Monthly) or 'PREMIUM' (Annual)");
        }

        // Get subscription details based on plan
        SubscriptionDetails details = getSubscriptionDetails(request.getPlan());

        // Create pending subscription
        Subscription subscription = Subscription.builder()
                .vendorId(vendorId)
                .plan(SubscriptionPlan.valueOf(request.getPlan().toUpperCase()))
                .amount(details.amount)
                .billingCycle(details.billingCycle)
                .status(SubscriptionStatus.PENDING)
                .startedAt(null)
                .expiresAt(null)
                .build();

        subscriptionRepository.save(subscription);

        // Initialize Paystack payment
        String reference = "SUB-" + vendorId.toString().substring(0, 8) + "-" + System.currentTimeMillis();

        InitiatePaymentResponse paymentResponse = paystackClient.initializeTransaction(
                userEmail,
                details.amount,
                reference,
                request.getCallbackUrl()
        );

        String cycleText = details.billingCycle == BillingCycle.ANNUAL ? "Annual" : "Monthly";
        log.info("💳 Subscription payment initiated for vendor: {} - Plan: {} ({}) - Amount: ₦{}",
                vendorId, request.getPlan(), cycleText, details.amount);

        return paymentResponse;
    }

    /**
     * Confirm subscription payment and activate subscription
     */
    @Transactional
    public SubscriptionDto confirmSubscriptionPayment(String reference) throws BusinessException {
        // Verify payment with Paystack
        boolean verified = paystackClient.verifyTransaction(reference);

        if (!verified) {
            throw new BusinessException("Payment verification failed");
        }

        // Find the pending subscription
        Subscription pendingSubscription = subscriptionRepository.findAll().stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.PENDING)
                .findFirst()
                .orElseThrow(() -> new BusinessException("No pending subscription found"));

        // Activate the subscription
        return activateSubscription(pendingSubscription);
    }

    /**
     * Activate subscription after successful payment
     */
    @Transactional
    public SubscriptionDto activateSubscription(Subscription pendingSubscription) {
        UUID vendorId = pendingSubscription.getVendorId();
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

        // Get days based on plan
        int days = pendingSubscription.getPlan() == SubscriptionPlan.PREMIUM ? 365 : 30;

        Instant now = Instant.now();
        Instant expiresAt = now.plus(days, ChronoUnit.DAYS);

        // Update subscription
        pendingSubscription.setStatus(SubscriptionStatus.ACTIVE);
        pendingSubscription.setStartedAt(now);
        pendingSubscription.setExpiresAt(expiresAt);

        Subscription activated = subscriptionRepository.save(pendingSubscription);

        // Update vendor
        vendor.setSubscriptionStatus("ACTIVE");
        vendor.setAccountStatus(VendorAccountStatus.ENABLED);
        vendorRepository.save(vendor);

        String cycleText = pendingSubscription.getBillingCycle() == BillingCycle.ANNUAL ? "Annual" : "Monthly";
        log.info("✅ Subscription activated for vendor: {} - Plan: {} ({}) - Amount: ₦{}",
                vendorId, pendingSubscription.getPlan(), cycleText, pendingSubscription.getAmount());

        // Send confirmation email
        sendSubscriptionConfirmationEmail(vendorId, activated);

        // Send push notification
        String planName = pendingSubscription.getPlan().name();
        String expiryText = pendingSubscription.getBillingCycle() == BillingCycle.ANNUAL ? "1 year" : "1 month";
        sendSubscriptionNotification(vendorId, "🎉 Subscription Activated!",
                "Your " + planName + " " + cycleText + " subscription is now active! Valid for " + expiryText + ". Expires on: " + expiresAt);

        return toDto(activated);
    }

    /**
     * Get current subscription for vendor
     */
    public SubscriptionDto getMySubscription(UUID vendorId) {
        return subscriptionRepository.findTopByVendorIdAndStatusOrderByExpiresAtDesc(vendorId, SubscriptionStatus.ACTIVE)
                .or(() -> subscriptionRepository.findTopByVendorIdAndStatusOrderByExpiresAtDesc(vendorId, SubscriptionStatus.FREE_TRIAL))
                .map(this::toDto)
                .orElse(null);
    }

    /**
     * Check if vendor has an active subscription
     */
    public boolean hasActiveSubscription(UUID vendorId) {
        return subscriptionRepository.existsByVendorIdAndStatus(vendorId, SubscriptionStatus.ACTIVE) ||
                subscriptionRepository.existsByVendorIdAndStatus(vendorId, SubscriptionStatus.FREE_TRIAL);
    }

    /**
     * Get all subscriptions (Admin only)
     */
    public List<SubscriptionDto> getAllSubscriptions() {
        return subscriptionRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // ========== SCHEDULED JOBS ==========

    @Scheduled(cron = "0 0 9 * * *")
    @Transactional
    public void checkExpiringSubscriptions() {
        log.info("🔍 Checking for expiring subscriptions...");

        Instant now = Instant.now();
        Instant fiveDaysFromNow = now.plus(EXPIRY_REMINDER_DAYS, ChronoUnit.DAYS);

        List<Subscription> expiringSoon = subscriptionRepository.findSubscriptionsExpiringBetween(
                SubscriptionStatus.ACTIVE, fiveDaysFromNow, fiveDaysFromNow.plus(1, ChronoUnit.DAYS));

        for (Subscription subscription : expiringSoon) {
            sendExpiryReminder(subscription);
        }

        log.info("✅ Expiring subscriptions check completed. Found {} expiring soon", expiringSoon.size());
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void expireSubscriptions() {
        log.info("🔍 Checking for expired subscriptions...");

        Instant now = Instant.now();

        List<Subscription> expiredSubscriptions = subscriptionRepository.findByStatusAndExpiresAtBefore(
                SubscriptionStatus.ACTIVE, now);

        expiredSubscriptions.addAll(
                subscriptionRepository.findByStatusAndExpiresAtBefore(
                        SubscriptionStatus.FREE_TRIAL, now)
        );

        for (Subscription subscription : expiredSubscriptions) {
            expireSubscription(subscription);
        }

        log.info("✅ Expired {} subscriptions", expiredSubscriptions.size());
    }

    @Transactional
    public void expireSubscription(Subscription subscription) {
        subscription.setStatus(SubscriptionStatus.EXPIRED);
        subscriptionRepository.save(subscription);

        UUID vendorId = subscription.getVendorId();
        Vendor vendor = vendorRepository.findById(vendorId).orElse(null);

        if (vendor != null) {
            vendor.setSubscriptionStatus("EXPIRED");
            vendor.setAccountStatus(VendorAccountStatus.DISABLED);
            vendor.setAccountDisabledReason("Subscription expired. Please renew to continue.");
            vendor.setOpen(false);
            vendorRepository.save(vendor);

            log.info("⛔ Vendor account disabled due to subscription expiry: {}", vendorId);

            sendSubscriptionNotification(vendorId, "⚠️ Subscription Expired",
                    "Your subscription has expired. Please renew to continue accepting orders.");

            sendSubscriptionExpiredEmail(vendorId);
        }
    }

    // ========== EMAIL NOTIFICATIONS ==========

    private void sendFreeTrialEmail(UUID vendorId, Instant expiresAt) {
        Vendor vendor = vendorRepository.findById(vendorId).orElse(null);
        if (vendor == null) return;

        String userEmail = getVendorEmail(vendorId);
        String userName = getVendorName(vendorId);

        if (userEmail == null) {
            log.warn("No user email found for vendor: {}", vendorId);
            return;
        }

        String subject = "🎉 Your 30-Day Free Trial is Active!";
        String content = String.format("""
            <html>
            <body>
                <h2>Welcome to GasLink Vendor Platform!</h2>
                <p>Dear %s,</p>
                <p>Your 30-day free trial has been activated! You now have full access to:</p>
                <ul>
                    <li>📦 Manage your inventory</li>
                    <li>📋 Accept and process orders</li>
                    <li>📊 Track your earnings</li>
                    <li>⭐ Get customer reviews</li>
                </ul>
                <p><strong>Your free trial expires on: %s</strong></p>
                <p>After the trial, choose a subscription plan:</p>
                <ul>
                    <li><strong>BASIC:</strong> ₦5,000/month (Monthly subscription)</li>
                    <li><strong>PREMIUM:</strong> ₦50,000/year (Annual subscription - Save 17%!)</li>
                </ul>
                <p>Best regards,<br/>GasLink Team</p>
            </body>
            </html>
            """,
                userName,
                expiresAt
        );

        try {
            emailService.sendEmail(userEmail, subject, content);
        } catch (Exception e) {
            log.error("Failed to send free trial email: {}", e.getMessage());
        }
    }

    private void sendSubscriptionConfirmationEmail(UUID vendorId, Subscription subscription) {
        Vendor vendor = vendorRepository.findById(vendorId).orElse(null);
        if (vendor == null) return;

        String userEmail = getVendorEmail(vendorId);
        String userName = getVendorName(vendorId);

        if (userEmail == null) {
            log.warn("No user email found for vendor: {}", vendorId);
            return;
        }

        String cycleText = subscription.getBillingCycle() == BillingCycle.ANNUAL ? "Annual" : "Monthly";
        String duration = subscription.getBillingCycle() == BillingCycle.ANNUAL ? "1 year" : "1 month";

        String subject = "✅ Subscription Confirmed - GasLink";
        String content = String.format("""
            <html>
            <body>
                <h2>Subscription Activated Successfully!</h2>
                <p>Dear %s,</p>
                <p>Your <strong>%s</strong> subscription has been activated.</p>
                <p><strong>Plan:</strong> %s</p>
                <p><strong>Billing Cycle:</strong> %s</p>
                <p><strong>Amount:</strong> ₦%s</p>
                <p><strong>Valid for:</strong> %s</p>
                <p><strong>Expires on:</strong> %s</p>
                <p>Thank you for choosing GasLink!</p>
                <p>Best regards,<br/>GasLink Team</p>
            </body>
            </html>
            """,
                userName,
                subscription.getPlan(),
                subscription.getPlan(),
                cycleText,
                subscription.getAmount(),
                duration,
                subscription.getExpiresAt()
        );

        try {
            emailService.sendEmail(userEmail, subject, content);
        } catch (Exception e) {
            log.error("Failed to send subscription confirmation email: {}", e.getMessage());
        }
    }

    private void sendExpiryReminder(Subscription subscription) {
        UUID vendorId = subscription.getVendorId();
        Vendor vendor = vendorRepository.findById(vendorId).orElse(null);

        if (vendor != null) {
            String cycleText = subscription.getBillingCycle() == BillingCycle.ANNUAL ? "Annual" : "Monthly";
            String message = String.format(
                    "Your %s %s subscription expires in %d days on %s. Please renew to avoid service interruption.",
                    subscription.getPlan(),
                    cycleText,
                    EXPIRY_REMINDER_DAYS,
                    subscription.getExpiresAt()
            );

            sendExpiryReminderEmail(vendorId, subscription);
            sendSubscriptionNotification(vendorId, "⏰ Subscription Expiring Soon!", message);

            log.info("📧 Expiry reminder sent to vendor: {}", vendorId);
        }
    }

    private void sendExpiryReminderEmail(UUID vendorId, Subscription subscription) {
        Vendor vendor = vendorRepository.findById(vendorId).orElse(null);
        if (vendor == null) return;

        String userEmail = getVendorEmail(vendorId);
        String businessName = vendor.getBusinessName();

        if (userEmail == null) {
            log.warn("No user email found for vendor: {}", vendorId);
            return;
        }

        String cycleText = subscription.getBillingCycle() == BillingCycle.ANNUAL ? "Annual" : "Monthly";

        String subject = "⏰ Subscription Expiring Soon!";
        String content = String.format("""
            <html>
            <body>
                <h2>Subscription Expiry Reminder</h2>
                <p>Dear %s,</p>
                <p>Your <strong>%s %s</strong> subscription will expire in <strong>5 days</strong> on <strong>%s</strong>.</p>
                <p>Please renew your subscription to continue using GasLink vendor services.</p>
                <p><strong>Renewal Options:</strong></p>
                <ul>
                    <li><strong>BASIC:</strong> ₦5,000/month (Monthly subscription)</li>
                    <li><strong>PREMIUM:</strong> ₦50,000/year (Annual subscription - Save 17%!)</li>
                </ul>
                <p><a href="%s/subscribe">Click here to renew now</a></p>
                <p>Best regards,<br/>GasLink Team</p>
            </body>
            </html>
            """,
                businessName,
                subscription.getPlan(),
                cycleText,
                subscription.getExpiresAt(),
                "https://gaslink.com"
        );

        try {
            emailService.sendEmail(userEmail, subject, content);
        } catch (Exception e) {
            log.error("Failed to send expiry reminder email: {}", e.getMessage());
        }
    }

    private void sendSubscriptionExpiredEmail(UUID vendorId) {
        Vendor vendor = vendorRepository.findById(vendorId).orElse(null);
        if (vendor == null) return;

        String userEmail = getVendorEmail(vendorId);
        String businessName = vendor.getBusinessName();

        if (userEmail == null) {
            log.warn("No user email found for vendor: {}", vendorId);
            return;
        }

        String subject = "⚠️ Subscription Expired - GasLink";
        String content = String.format("""
            <html>
            <body>
                <h2>Subscription Expired</h2>
                <p>Dear %s,</p>
                <p>Your subscription has expired. Your account has been temporarily disabled.</p>
                <p>To continue using GasLink vendor services, please renew your subscription.</p>
                <p><strong>Renewal Options:</strong></p>
                <ul>
                    <li><strong>BASIC:</strong> ₦5,000/month (Monthly subscription)</li>
                    <li><strong>PREMIUM:</strong> ₦50,000/year (Annual subscription - Save 17%!)</li>
                </ul>
                <p><a href="%s/subscribe">Click here to renew now</a></p>
                <p>Best regards,<br/>GasLink Team</p>
            </body>
            </html>
            """,
                businessName,
                "https://gaslink.com"
        );

        try {
            emailService.sendEmail(userEmail, subject, content);
        } catch (Exception e) {
            log.error("Failed to send subscription expired email: {}", e.getMessage());
        }
    }

    private void sendSubscriptionNotification(UUID vendorId, String title, String body) {
        try {
            notificationService.notify(vendorId, "SUBSCRIPTION", title, body);
        } catch (Exception e) {
            log.error("Failed to send push notification to vendor {}: {}", vendorId, e.getMessage());
        }
    }

    private SubscriptionDto toDto(Subscription s) {
        if (s == null) return null;

        return SubscriptionDto.builder()
                .id(s.getId())
                .vendorId(s.getVendorId())
                .plan(s.getPlan() != null ? s.getPlan().name() : null)
                .amount(s.getAmount())
                .billingCycle(s.getBillingCycle() != null ? s.getBillingCycle().name() : null)
                .status(s.getStatus())
                .startedAt(s.getStartedAt())
                .expiresAt(s.getExpiresAt())
                .build();
    }
}