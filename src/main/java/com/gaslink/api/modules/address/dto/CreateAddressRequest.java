//package com.gaslink.api.modules.address.dto;
//import jakarta.validation.constraints.*;
//import lombok.Data;
//
//@Data
//public class CreateAddressRequest {
//    @NotBlank private String label;
//    @NotBlank private String addressLine;
//    @NotBlank private String city;
//    @NotBlank private String state;
//    private Double lat;
//    private Double lng;
//
//    public @NotBlank String getLabel() {
//        return label;
//    }
//
//    public void setLabel(@NotBlank String label) {
//        this.label = label;
//    }
//
//    public @NotBlank String getAddressLine() {
//        return addressLine;
//    }
//
//    public void setAddressLine(@NotBlank String addressLine) {
//        this.addressLine = addressLine;
//    }
//
//    public @NotBlank String getCity() {
//        return city;
//    }
//
//    public void setCity(@NotBlank String city) {
//        this.city = city;
//    }
//
//    public @NotBlank String getState() {
//        return state;
//    }
//
//    public void setState(@NotBlank String state) {
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
//    private boolean isDefault = false;
//}