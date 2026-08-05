//package com.gaslink.api.modules.inventory.dto;
//
//import java.math.BigDecimal;
//import java.util.UUID;
//
//public class InventoryDto {
//    private UUID id;
//    private UUID vendorId;
//    private Double sizeKg;
//    private int availableQty;
//    private BigDecimal refillPrice;
//    private BigDecimal exchangePrice;
//    private BigDecimal rentalPricePerDay;
//    private BigDecimal purchasePrice;
//
//    // 1. MANUAL NO-ARGS CONSTRUCTOR (For JSON)
//    public InventoryDto() {}
//
//    // 2. MANUAL ALL-ARGS CONSTRUCTOR (Replaces the Builder)
//    public InventoryDto(UUID id, UUID vendorId, Double sizeKg, int availableQty,
//                        BigDecimal refillPrice, BigDecimal exchangePrice,
//                        BigDecimal rentalPricePerDay, BigDecimal purchasePrice) {
//        this.id = id;
//        this.vendorId = vendorId;
//        this.sizeKg = sizeKg;
//        this.availableQty = availableQty;
//        this.refillPrice = refillPrice;
//        this.exchangePrice = exchangePrice;
//        this.rentalPricePerDay = rentalPricePerDay;
//        this.purchasePrice = purchasePrice;
//    }
//
//    // 3. MANUAL GETTERS AND SETTERS
//    public UUID getId() { return id; }
//    public void setId(UUID id) { this.id = id; }
//    public UUID getVendorId() { return vendorId; }
//    public void setVendorId(UUID vendorId) { this.vendorId = vendorId; }
//    public Double getSizeKg() { return sizeKg; }
//    public void setSizeKg(Double sizeKg) { this.sizeKg = sizeKg; }
//    public int getAvailableQty() { return availableQty; }
//    public void setAvailableQty(int availableQty) { this.availableQty = availableQty; }
//    public BigDecimal getRefillPrice() { return refillPrice; }
//    public void setRefillPrice(BigDecimal refillPrice) { this.refillPrice = refillPrice; }
//    public BigDecimal getExchangePrice() { return exchangePrice; }
//    public void setExchangePrice(BigDecimal exchangePrice) { this.exchangePrice = exchangePrice; }
//    public BigDecimal getRentalPricePerDay() { return rentalPricePerDay; }
//    public void setRentalPricePerDay(BigDecimal rentalPricePerDay) { this.rentalPricePerDay = rentalPricePerDay; }
//    public BigDecimal getPurchasePrice() { return purchasePrice; }
//    public void setPurchasePrice(BigDecimal purchasePrice) { this.purchasePrice = purchasePrice; }
//}