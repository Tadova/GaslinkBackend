//package com.gaslink.api.modules.address;
//import com.gaslink.api.modules.address.dto.*;
//import com.gaslink.api.shared.response.ApiResponse;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.*;
//import org.springframework.security.core.Authentication;
//import org.springframework.web.bind.annotation.*;
//import java.util.*;
//
//@RestController
//@RequestMapping("/api/v1/addresses")
//
//public class AddressController {
//    private final AddressService addressService;
//
//    public AddressController(AddressService addressService) {
//        this.addressService = addressService;
//    }
//
//    @GetMapping
//    public ResponseEntity<ApiResponse<List<AddressDto>>> getAll(Authentication auth) {
//        return ResponseEntity.ok(ApiResponse.success(addressService.getAddresses(UUID.fromString(auth.getName()))));
//    }
//    @PostMapping
//    public ResponseEntity<ApiResponse<AddressDto>> create(Authentication auth, @Valid @RequestBody CreateAddressRequest req) {
//        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(addressService.createAddress(UUID.fromString(auth.getName()), req)));
//    }
//    @PutMapping("/{id}")
//    public ResponseEntity<ApiResponse<AddressDto>> update(Authentication auth, @PathVariable UUID id, @Valid @RequestBody CreateAddressRequest req) {
//        return ResponseEntity.ok(ApiResponse.success(addressService.updateAddress(UUID.fromString(auth.getName()), id, req)));
//    }
//    @DeleteMapping("/{id}")
//    public ResponseEntity<ApiResponse<Void>> delete(Authentication auth, @PathVariable UUID id) {
//        addressService.deleteAddress(UUID.fromString(auth.getName()), id);
//        return ResponseEntity.ok(ApiResponse.success("Address deleted.", null));
//    }
//    @PatchMapping("/{id}/default")
//    public ResponseEntity<ApiResponse<AddressDto>> setDefault(Authentication auth, @PathVariable UUID id) {
//        return ResponseEntity.ok(ApiResponse.success(addressService.setDefault(UUID.fromString(auth.getName()), id)));
//    }
//}