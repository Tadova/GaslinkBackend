package com.gaslink.api.scheduler;

import com.gaslink.api.modules.subscription.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionExpiryScheduler {

    private final SubscriptionService subscriptionService;

    /**
     * Expire subscriptions daily at midnight (12:00 AM)
     * This runs the expireSubscriptions() method in SubscriptionService
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void expireSubscriptions() {
        log.info("⏰ Running subscription expiry check...");
        subscriptionService.expireSubscriptions();
        log.info("✅ Subscription expiry check completed");
    }

    /**
     * Send expiry reminders daily at 9:00 AM
     * This sends reminders to vendors whose subscriptions expire in 5 days
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void checkExpiringSubscriptions() {
        log.info("⏰ Running subscription expiry reminder check...");
        subscriptionService.checkExpiringSubscriptions();
        log.info("✅ Subscription expiry reminder check completed");
    }
}