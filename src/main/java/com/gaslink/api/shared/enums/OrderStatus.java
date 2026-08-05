package com.gaslink.api.shared.enums;

public enum OrderStatus {
    PENDING,        // Order placed, waiting for vendor to accept
    ACCEPTED,       // Vendor accepted the order
    REJECTED,       // Vendor rejected the order
    PROCESSING,     // Vendor is processing the order
    READY,          // Order is ready for pickup/delivery
    COMPLETED,      // Order completed
    CANCELLED       // Order cancelled by customer
}