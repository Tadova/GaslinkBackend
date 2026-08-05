//package com.gaslink.api.modules.address;
//import com.gaslink.api.shared.audit.AuditableEntity;
//import jakarta.persistence.*;
//import lombok.*;
//import java.util.UUID;
//
//@Entity
//@Table(name = "addresses")
//@Data
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//@EqualsAndHashCode(callSuper = false)
//
//public class Address extends AuditableEntity {
//    @Id @GeneratedValue @Column(columnDefinition = "uuid") private UUID id;
//    @Column(name = "user_id", nullable = false) private UUID userId;
//    private String label;
//    @Column(name = "address_line", nullable = false) private String addressLine;
//    private String city;
//    private String state;
//    private Double lat;
//    private Double lng;
//    @Column(name = "is_default") private boolean isDefault = false;
//
//    public UUID getId() {
//        return id;
//    }
//
//    public void setId(UUID id) {
//        this.id = id;
//    }
//
//    public UUID getUserId() {
//        return userId;
//    }
//
//    public void setUserId(UUID userId) {
//        this.userId = userId;
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
//}