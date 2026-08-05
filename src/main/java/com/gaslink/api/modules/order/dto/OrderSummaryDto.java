//package com.gaslink.api.modules.order.dto;
//import com.gaslink.api.shared.enums.*;
//import lombok.*;
//import java.math.BigDecimal;
//import java.time.Instant;
//import java.util.UUID;
//
//@Data
////@Builder
////@NoArgsConstructor
////@AllArgsConstructor
//public class OrderSummaryDto {
//    private UUID id;
//    private String reference;
//    private OrderStatus status;
//    private ServiceType serviceType;
//    private Double cylinderSizeKg;
//    private int qty;
//    private BigDecimal total;
//    private Instant createdAt;
//
//
//    public OrderSummaryDto(UUID id, String reference, OrderStatus status,
//                           ServiceType serviceType, Double cylinderSizeKg,
//                           Integer qty, BigDecimal total, Instant createdAt) {
//        this.id = id;
//        this.reference = reference;
//        this.status = status;
//        this.serviceType = serviceType;
//        this.cylinderSizeKg = cylinderSizeKg;
//        this.qty = qty;
//        this.total = total;
//        this.createdAt = createdAt;
//    }
//
//    public UUID getId() {
//        return id;
//    }
//
//    public void setId(UUID id) {
//        this.id = id;
//    }
//
//    public String getReference() {
//        return reference;
//    }
//
//    public void setReference(String reference) {
//        this.reference = reference;
//    }
//
//    public OrderStatus getStatus() {
//        return status;
//    }
//
//    public void setStatus(OrderStatus status) {
//        this.status = status;
//    }
//
//    public ServiceType getServiceType() {
//        return serviceType;
//    }
//
//    public void setServiceType(ServiceType serviceType) {
//        this.serviceType = serviceType;
//    }
//
//    public Double getCylinderSizeKg() {
//        return cylinderSizeKg;
//    }
//
//    public void setCylinderSizeKg(Double cylinderSizeKg) {
//        this.cylinderSizeKg = cylinderSizeKg;
//    }
//
//    public int getQty() {
//        return qty;
//    }
//
//    public void setQty(int qty) {
//        this.qty = qty;
//    }
//
//    public BigDecimal getTotal() {
//        return total;
//    }
//
//    public void setTotal(BigDecimal total) {
//        this.total = total;
//    }
//
//    public Instant getCreatedAt() {
//        return createdAt;
//    }
//
//    public void setCreatedAt(Instant createdAt) {
//        this.createdAt = createdAt;
//    }
//}