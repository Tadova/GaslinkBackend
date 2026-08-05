//package com.gaslink.api.modules.order.dto;
//
//import com.gaslink.api.shared.enums.OrderStatus;
//import com.gaslink.api.shared.enums.PaymentMethod;
//import com.gaslink.api.shared.enums.PaymentStatus;
//import com.gaslink.api.shared.enums.ServiceType;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//import java.math.BigDecimal;
//import java.time.Instant;
//import java.util.List;
//import java.util.UUID;
//
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//public class OrderDto {
//    private UUID id;
//    private String reference;
//    private UUID customerId;
//    private UUID vendorId;
//    private ServiceType serviceType;
//    private OrderStatus status;
//    private Double cylinderSizeKg;
//    private Integer qty;
//    private Double deliveryLat;
//    private Double deliveryLng;
//    private Double distanceKm;
//    private BigDecimal subtotal;
//    private BigDecimal deliveryFee;
//    private BigDecimal platformFee;
//    private BigDecimal total;
//    private PaymentMethod paymentMethod;
//    private PaymentStatus paymentStatus;
//    private Instant scheduledAt;
//    private Instant createdAt;
//
//    // Added by toDtoWithHistory()
//    private List<StatusHistoryDto> history;
//
//    public OrderDto(UUID id, String reference, UUID customerId, UUID vendorId, ServiceType serviceType, OrderStatus status, Double cylinderSizeKg, Integer qty, Double deliveryLat, Double deliveryLng, Double distanceKm, BigDecimal subtotal, BigDecimal deliveryFee, BigDecimal platformFee, BigDecimal total, PaymentMethod paymentMethod, PaymentStatus paymentStatus, Instant scheduledAt, Instant createdAt) {
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
//    public UUID getCustomerId() {
//        return customerId;
//    }
//
//    public void setCustomerId(UUID customerId) {
//        this.customerId = customerId;
//    }
//
//    public UUID getVendorId() {
//        return vendorId;
//    }
//
//    public void setVendorId(UUID vendorId) {
//        this.vendorId = vendorId;
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
//    public OrderStatus getStatus() {
//        return status;
//    }
//
//    public void setStatus(OrderStatus status) {
//        this.status = status;
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
//    public Integer getQty() {
//        return qty;
//    }
//
//    public void setQty(Integer qty) {
//        this.qty = qty;
//    }
//
//    public Double getDeliveryLat() {
//        return deliveryLat;
//    }
//
//    public void setDeliveryLat(Double deliveryLat) {
//        this.deliveryLat = deliveryLat;
//    }
//
//    public Double getDeliveryLng() {
//        return deliveryLng;
//    }
//
//    public void setDeliveryLng(Double deliveryLng) {
//        this.deliveryLng = deliveryLng;
//    }
//
//    public Double getDistanceKm() {
//        return distanceKm;
//    }
//
//    public void setDistanceKm(Double distanceKm) {
//        this.distanceKm = distanceKm;
//    }
//
//    public BigDecimal getSubtotal() {
//        return subtotal;
//    }
//
//    public void setSubtotal(BigDecimal subtotal) {
//        this.subtotal = subtotal;
//    }
//
//    public BigDecimal getDeliveryFee() {
//        return deliveryFee;
//    }
//
//    public void setDeliveryFee(BigDecimal deliveryFee) {
//        this.deliveryFee = deliveryFee;
//    }
//
//    public BigDecimal getPlatformFee() {
//        return platformFee;
//    }
//
//    public void setPlatformFee(BigDecimal platformFee) {
//        this.platformFee = platformFee;
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
//    public PaymentMethod getPaymentMethod() {
//        return paymentMethod;
//    }
//
//    public void setPaymentMethod(PaymentMethod paymentMethod) {
//        this.paymentMethod = paymentMethod;
//    }
//
//    public PaymentStatus getPaymentStatus() {
//        return paymentStatus;
//    }
//
//    public void setPaymentStatus(PaymentStatus paymentStatus) {
//        this.paymentStatus = paymentStatus;
//    }
//
//    public Instant getScheduledAt() {
//        return scheduledAt;
//    }
//
//    public void setScheduledAt(Instant scheduledAt) {
//        this.scheduledAt = scheduledAt;
//    }
//
//    public Instant getCreatedAt() {
//        return createdAt;
//    }
//
//    public void setCreatedAt(Instant createdAt) {
//        this.createdAt = createdAt;
//    }
//
//    public List<StatusHistoryDto> getHistory() {
//        return history;
//    }
//
//    public void setHistory(List<StatusHistoryDto> history) {
//        this.history = history;
//    }
//}