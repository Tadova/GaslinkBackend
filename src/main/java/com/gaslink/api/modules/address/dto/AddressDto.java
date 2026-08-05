//package com.gaslink.api.modules.address.dto;
//import lombok.*;
//import java.util.UUID;
//
//@Data
//@NoArgsConstructor
//
//public class AddressDto {
//    private UUID id;
//    private String label;
//    private String addressLine;
//    private String city;
//    private String state;
//    private Double lat;
//
//    public UUID getId() {
//        return id;
//    }
//
//    public void setId(UUID id) {
//        this.id = id;
//    }
//
//    public String getLabel() {
//        return label;
//    }
//
//    public void setLabel(String label) {
//        this.label = label;
//    }
//
//    public String getAddressLine() {
//        return addressLine;
//    }
//
//    public void setAddressLine(String addressLine) {
//        this.addressLine = addressLine;
//    }
//
//    public String getCity() {
//        return city;
//    }
//
//    public void setCity(String city) {
//        this.city = city;
//    }
//
//    public String getState() {
//        return state;
//    }
//
//    public void setState(String state) {
//        this.state = state;
//    }
//
//    public Double getLat() {
//        return lat;
//    }
//
//    public void setLat(Double lat) {
//        this.lat = lat;
//    }
//
//    public Double getLng() {
//        return lng;
//    }
//
//    public void setLng(Double lng) {
//        this.lng = lng;
//    }
//
//    public boolean isDefault() {
//        return isDefault;
//    }
//
//    public void setDefault(boolean aDefault) {
//        isDefault = aDefault;
//    }
//
//    private Double lng;
//    private boolean isDefault;
//}