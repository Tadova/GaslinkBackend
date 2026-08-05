//package com.gaslink.api.modules.address;
//import com.gaslink.api.modules.address.dto.AddressDto;
//import com.gaslink.api.modules.address.dto.CreateAddressRequest;
//import com.gaslink.api.shared.exception.*;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Service
//
//public class AddressService {
//    private final AddressRepository addressRepository;
//
//    public AddressService(AddressRepository addressRepository) {
//        this.addressRepository = addressRepository;
//    }
//
//    public List<AddressDto> getAddresses(UUID userId) {
//        return addressRepository.findByUserId(userId).stream().map(this::toDto).collect(Collectors.toList());
//    }
//
//    @Transactional
//    public AddressDto createAddress(UUID userId, CreateAddressRequest req) {
//        if (req.isDefault()) addressRepository.clearDefaultForUser(userId);
//
//        // REPLACED Address.builder() with manual creation
//        Address a = new Address();
//        a.setUserId(userId);
//        a.setLabel(req.getLabel());
//        a.setAddressLine(req.getAddressLine());
//        a.setCity(req.getCity());
//        a.setState(req.getState());
//        a.setLat(req.getLat());
//        a.setLng(req.getLng());
//        a.setDefault(req.isDefault());
//
//        return toDto(addressRepository.save(a));
//    }
//
//    @Transactional
//    public AddressDto updateAddress(UUID userId, UUID id, CreateAddressRequest req) {
//        Address a = addressRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Address not found"));
//        if (!a.getUserId().equals(userId)) throw new ForbiddenException("Access denied");
//        if (req.isDefault()) addressRepository.clearDefaultForUser(userId);
//        a.setLabel(req.getLabel()); a.setAddressLine(req.getAddressLine());
//        a.setCity(req.getCity()); a.setState(req.getState());
//        a.setLat(req.getLat()); a.setLng(req.getLng()); a.setDefault(req.isDefault());
//        return toDto(addressRepository.save(a));
//    }
//
//    @Transactional
//    public void deleteAddress(UUID userId, UUID id) {
//        Address a = addressRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Address not found"));
//        if (!a.getUserId().equals(userId)) throw new ForbiddenException("Access denied");
//        addressRepository.delete(a);
//    }
//
//    @Transactional
//    public AddressDto setDefault(UUID userId, UUID id) {
//        addressRepository.clearDefaultForUser(userId);
//        Address a = addressRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Address not found"));
//        if (!a.getUserId().equals(userId)) throw new ForbiddenException("Access denied");
//        a.setDefault(true);
//        return toDto(addressRepository.save(a));
//    }
//
//    private AddressDto toDto(Address a) {
//        // REPLACED AddressDto.builder() with manual creation
//        AddressDto dto = new AddressDto();
//        dto.setId(a.getId());
//        dto.setLabel(a.getLabel());
//        dto.setAddressLine(a.getAddressLine());
//        dto.setCity(a.getCity());
//        dto.setState(a.getState());
//        dto.setLat(a.getLat());
//        dto.setLng(a.getLng());
//        dto.setDefault(a.isDefault());
//        return dto;
//    }
//}