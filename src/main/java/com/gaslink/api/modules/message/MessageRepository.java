package com.gaslink.api.modules.message;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {

    // Get messages between two users for a specific order
    List<Message> findByOrderIdOrderByCreatedAtAsc(UUID orderId);

    Page<Message> findByOrderId(UUID orderId, Pageable pageable);

    // Get unread messages count
    @Query("SELECT COUNT(m) FROM Message m WHERE m.receiverId = :userId AND m.isRead = false")
    long countUnreadMessages(@Param("userId") UUID userId);

    // Get unread messages by order
    @Query("SELECT COUNT(m) FROM Message m WHERE m.orderId = :orderId AND m.receiverId = :userId AND m.isRead = false")
    long countUnreadMessagesByOrder(@Param("orderId") UUID orderId, @Param("userId") UUID userId);

    // Get last message for each order
    @Query("SELECT m FROM Message m WHERE m.orderId = :orderId ORDER BY m.createdAt DESC LIMIT 1")
    Optional<Message> findLastMessageByOrder(@Param("orderId") UUID orderId);

    // Mark messages as read
    @Modifying
    @Query("UPDATE Message m SET m.isRead = true, m.readAt = CURRENT_TIMESTAMP WHERE m.orderId = :orderId AND m.receiverId = :userId AND m.isRead = false")
    void markAllAsRead(@Param("orderId") UUID orderId, @Param("userId") UUID userId);
}