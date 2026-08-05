//package com.gaslink.api.modules.order.dto;
//
//import com.gaslink.api.shared.enums.OrderStatus;
//import java.time.Instant;
//import java.util.UUID;
//
//public class StatusHistoryDto {
//
//    private OrderStatus status;
//    private UUID changedBy;
//    private String note;
//    private Instant createdAt;
//
//    public StatusHistoryDto() {}
//
//    public StatusHistoryDto(OrderStatus status, UUID changedBy, String note, Instant createdAt) {
//        this.status    = status;
//        this.changedBy = changedBy;
//        this.note      = note;
//        this.createdAt = createdAt;
//    }
//
//    // Getters & setters
//    public OrderStatus getStatus()       { return status; }
//    public void setStatus(OrderStatus s) { this.status = s; }
//
//    public UUID getChangedBy()           { return changedBy; }
//    public void setChangedBy(UUID id)    { this.changedBy = id; }
//
//    public String getNote()              { return note; }
//    public void setNote(String note)     { this.note = note; }
//
//    public Instant getCreatedAt()        { return createdAt; }
//    public void setCreatedAt(Instant t)  { this.createdAt = t; }
//}