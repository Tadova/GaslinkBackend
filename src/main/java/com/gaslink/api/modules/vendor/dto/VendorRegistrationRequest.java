package com.gaslink.api.modules.vendor.dto;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class VendorRegistrationRequest {
    @NotBlank private String businessName;
    @NotBlank private String businessAddress;
    @NotBlank @Pattern(regexp = "^[0-9]{11}$", message = "NIN must be exactly 10 digits") private String nin;
    @NotNull private Double lat;
    @NotNull private Double lng;
    @NotNull @Min(1) @Max(100) private Double serviceRadiusKm;

    public @NotBlank String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(@NotBlank String businessName) {
        this.businessName = businessName;
    }

    public @NotBlank String getBusinessAddress() {
        return businessAddress;
    }

    public void setBusinessAddress(@NotBlank String businessAddress) {
        this.businessAddress = businessAddress;
    }

    public @NotBlank @Pattern(regexp = "^[0-9]{11}$", message = "NIN must be exactly 11 digits") String getNin() {
        return nin;
    }

    public void setNin(@NotBlank @Pattern(regexp = "^[0-9]{11}$", message = "NIN must be exactly 11 digits") String nin) {
        this.nin = nin;
    }

    public @NotNull Double getLat() {
        return lat;
    }

    public void setLat(@NotNull Double lat) {
        this.lat = lat;
    }

    public @NotNull Double getLng() {
        return lng;
    }

    public void setLng(@NotNull Double lng) {
        this.lng = lng;
    }

    public @NotNull @Min(1) @Max(100) Double getServiceRadiusKm() {
        return serviceRadiusKm;
    }

    public void setServiceRadiusKm(@NotNull @Min(1) @Max(100) Double serviceRadiusKm) {
        this.serviceRadiusKm = serviceRadiusKm;
    }
}