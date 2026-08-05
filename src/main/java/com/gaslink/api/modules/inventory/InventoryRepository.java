//package com.gaslink.api.modules.inventory;
//import org.springframework.data.jpa.repository.JpaRepository;
//import java.util.*;
//
//public interface InventoryRepository extends JpaRepository<CylinderInventory, UUID> {
//    List<CylinderInventory> findByVendorId(UUID vendorId);
//    Optional<CylinderInventory> findByVendorIdAndSizeKg(UUID vendorId, Double sizeKg);
//}