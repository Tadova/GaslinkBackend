package com.gaslink.api.modules.payment;
import com.gaslink.api.shared.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByOrderId(UUID orderId);
    Optional<Payment> findByGatewayRef(String ref);

    List<Payment> findByStatus(PaymentStatus paymentStatus);
}