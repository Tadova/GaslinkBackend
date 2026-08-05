//package com.gaslink.api.modules.inventory;
//
//import com.gaslink.api.shared.audit.AuditableEntity;
//import jakarta.persistence.*;
//import java.math.BigDecimal;
//import java.util.UUID;
//
//@Entity
//@Table(name = "cylinder_inventory")
//public class CylinderInventory extends AuditableEntity {
//    @Id
//    @GeneratedValue
//    @Column(columnDefinition = "uuid")
//    private UUID id;
//
//    @Column(name = "vendor_id", nullable = false)
//    private UUID vendorId;
//
//    @Column(name = "size_kg", nullable = false)
//    private Double sizeKg;
//
//    @Column(name = "available_qty")
//    private int availableQty = 0;
//
//    @Column(name = "refill_price", precision = 12, scale = 2)
//    private BigDecimal refillPrice;
//
//    @Column(name = "exchange_price", precision = 12, scale = 2)
//    private BigDecimal exchangePrice;
//
//    @Column(name = "rental_price_per_day", precision = 12, scale = 2)
//    private BigDecimal rentalPricePerDay;
//
//    @Column(name = "purchase_price", precision = 12, scale = 2)
//    private BigDecimal purchasePrice;
//
//    // 1. MANUAL NO-ARGS CONSTRUCTOR (Required by JPA)
//    public CylinderInventory() {}
//
//    // 2. MANUAL GETTERS
//    public UUID getId() { return id; }
//    public UUID getVendorId() { return vendorId; }
//    public Double getSizeKg() { return sizeKg; }
//    public int getAvailableQty() { return availableQty; }
//    public BigDecimal getRefillPrice() { return refillPrice; }
//    public BigDecimal getExchangePrice() { return exchangePrice; }
//    public BigDecimal getRentalPricePerDay() { return rentalPricePerDay; }
//    public BigDecimal getPurchasePrice() { return purchasePrice; }
//
//    // 3. MANUAL SETTERS
//    public void setId(UUID id) { this.id = id; }
//    public void setVendorId(UUID vendorId) { this.vendorId = vendorId; }
//    public void setSizeKg(Double sizeKg) { this.sizeKg = sizeKg; }
//    public void setAvailableQty(int availableQty) { this.availableQty = availableQty; }
//    public void setRefillPrice(BigDecimal refillPrice) { this.refillPrice = refillPrice; }
//    public void setExchangePrice(BigDecimal exchangePrice) { this.exchangePrice = exchangePrice; }
//    public void setRentalPricePerDay(BigDecimal rentalPricePerDay) { this.rentalPricePerDay = rentalPricePerDay; }
//    public void setPurchasePrice(BigDecimal purchasePrice) { this.purchasePrice = purchasePrice; }
//}