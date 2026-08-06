package com.gaslink.api.modules.subscription;

import com.gaslink.api.shared.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionPaymentRepository extends JpaRepository<SubscriptionPayment, UUID> {

    Optional<SubscriptionPayment> findByReference(String reference);

    List<SubscriptionPayment> findByVendorIdAndStatus(UUID vendorId, PaymentStatus status);

    boolean existsByReference(String reference);

    Optional<SubscriptionPayment> findBySubscriptionId(UUID subscriptionId);
}