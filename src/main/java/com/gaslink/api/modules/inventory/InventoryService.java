//package com.gaslink.api.modules.inventory;
//
//import com.gaslink.api.modules.inventory.dto.*;
//import com.gaslink.api.shared.exception.*;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Service
//public class InventoryService {
//    private final InventoryRepository inventoryRepository;
//
//    public InventoryService(InventoryRepository inventoryRepository) {
//        this.inventoryRepository = inventoryRepository;
//    }
//
//    public List<InventoryDto> getByVendor(UUID vendorId) {
//        return inventoryRepository.findByVendorId(vendorId).stream()
//                .map(this::toDto)
//                .collect(Collectors.toList());
//    }
//
//    @Transactional
//    public InventoryDto upsert(UUID vendorId, UpdateInventoryRequest req) {
//        // 1. Declare and initialize the 'inventory' variable
//        CylinderInventory inventory = new CylinderInventory();
//
//        // 2. Set the fields (Ensure req has manual getters! See Step 2 below)
//        inventory.setVendorId(vendorId);
//        inventory.setSizeKg(req.getSizeKg());
//        inventory.setRefillPrice(req.getRefillPrice());
//        inventory.setExchangePrice(req.getExchangePrice());
//        inventory.setPurchasePrice(req.getPurchasePrice());
//        inventory.setRentalPricePerDay(req.getRentalPricePerDay());
//        inventory.setAvailableQty(req.getAvailableQty());
//
//        // 3. Save it
//        return toDto(inventoryRepository.save(inventory));
//    }
//
//    @Transactional
//    public void delete(UUID vendorId, UUID inventoryId) {
//        CylinderInventory inv = inventoryRepository.findById(inventoryId)
//                .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found"));
//
//        if (!inv.getVendorId().equals(vendorId)) {
//            throw new ForbiddenException("Access denied");
//        }
//        inventoryRepository.delete(inv);
//    }
//
//    private InventoryDto toDto(CylinderInventory i) {
//        return new InventoryDto(
//                i.getId(),
//                i.getVendorId(),
//                i.getSizeKg(),
//                i.getAvailableQty(),
//                i.getRefillPrice(),
//                i.getExchangePrice(),
//                i.getRentalPricePerDay(),
//                i.getPurchasePrice()
//        );
//    }
//}