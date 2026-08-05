package com.gaslink.api.scheduler;

import com.gaslink.api.modules.order.*;
import com.gaslink.api.shared.enums.BidStatus;
import com.gaslink.api.shared.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutScheduler {

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final OrderBidRepository orderBidRepository; // Add this

    /**
     * Auto-cancel orders that have been pending for more than 5 minutes
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cancelTimedOutOrders() {
        Instant cutoff = Instant.now().minus(5, ChronoUnit.MINUTES);
        List<Order> staleOrders = orderRepository.findByStatusAndCreatedAtBefore(OrderStatus.PENDING, cutoff);

        for (Order order : staleOrders) {
            // Skip gas orders (they go through bidding)
            if (order.isGasOrder()) continue;

            if (order.getStatus() == OrderStatus.PENDING) {
                order.setStatus(OrderStatus.CANCELLED);
                order.setCancelledAt(Instant.now());
                order.setCancellationReason("Auto-cancelled: no vendor accepted within 5 minutes");
                orderRepository.save(order);

                OrderStatusHistory history = OrderStatusHistory.builder()
                        .orderId(order.getId())
                        .status(OrderStatus.CANCELLED)
                        .note("Auto-cancelled: no vendor accepted within 5 minutes")
                        .build();
                historyRepository.save(history);

                log.info("⏰ Auto-cancelled order {} - No vendor accepted within 5 minutes",
                        order.getOrderReference());
            }
        }
    }

    /**
     * Auto-cancel gas orders with no bids after bid deadline
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cancelGasOrdersWithNoBids() {
        Instant now = Instant.now();
        List<Order> expiredGasOrders = orderRepository.findByStatusAndBidDeadlineBefore(OrderStatus.PENDING, now);

        for (Order order : expiredGasOrders) {
            if (!order.isGasOrder()) continue;

            // Check if order has any pending bids
            long pendingBidCount = orderBidRepository.countByOrderIdAndStatus(order.getId(), BidStatus.PENDING);

            if (pendingBidCount == 0 && order.getSelectedBidId() == null) {
                order.setStatus(OrderStatus.CANCELLED);
                order.setCancelledAt(now);
                order.setCancellationReason("Auto-cancelled: no bids received before deadline");
                orderRepository.save(order);

                OrderStatusHistory history = OrderStatusHistory.builder()
                        .orderId(order.getId())
                        .status(OrderStatus.CANCELLED)
                        .note("Auto-cancelled: no bids received before deadline")
                        .build();
                historyRepository.save(history);

                log.info("⏰ Auto-cancelled gas order {} - No bids received",
                        order.getOrderReference());
            }
        }
    }
}