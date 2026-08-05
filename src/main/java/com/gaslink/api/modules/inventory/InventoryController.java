//package com.gaslink.api.modules.inventory;
//import com.gaslink.api.modules.inventory.dto.*;
//import com.gaslink.api.shared.response.ApiResponse;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.*;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.security.core.Authentication;
//import org.springframework.web.bind.annotation.*;
//import java.util.*;
//
//@RestController @RequestMapping("/api/v1/inventory")
//
//public class InventoryController {
//    private final InventoryService inventoryService;
//
//    public InventoryController(InventoryService inventoryService) {
//        this.inventoryService = inventoryService;
//    }
//
//    @GetMapping
//    @PreAuthorize("hasRole('VENDOR')")
//    public ResponseEntity<ApiResponse<List<InventoryDto>>> getMyInventory(Authentication auth) {
//        return ResponseEntity.ok(ApiResponse.success(inventoryService.getByVendor(UUID.fromString(auth.getName()))));
//    }
//
//    @GetMapping("/vendor/{vendorId}")
//    public ResponseEntity<ApiResponse<List<InventoryDto>>> getByVendor(@PathVariable UUID vendorId) {
//        return ResponseEntity.ok(ApiResponse.success(inventoryService.getByVendor(vendorId)));
//    }
//
//    @PostMapping
//    @PreAuthorize("hasRole('VENDOR')")
//    public ResponseEntity<ApiResponse<InventoryDto>> upsert(Authentication auth, @Valid @RequestBody UpdateInventoryRequest req) {
//        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(inventoryService.upsert(UUID.fromString(auth.getName()), req)));
//    }
//
//    @DeleteMapping("/{id}")
//    @PreAuthorize("hasRole('VENDOR')")
//    public ResponseEntity<ApiResponse<Void>> delete(Authentication auth, @PathVariable UUID id) {
//        inventoryService.delete(UUID.fromString(auth.getName()), id);
//        return ResponseEntity.ok(ApiResponse.success("Inventory item deleted.", null));
//    }
//}