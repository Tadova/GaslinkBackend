package com.gaslink.api.shared.enums;

public enum BidStatus {
    PENDING,        // Bid submitted, waiting for customer approval
    APPROVED,       // Customer approved this bid
    REJECTED,       // Customer rejected this bid
    EXPIRED         // Bid expired (timeout)
}