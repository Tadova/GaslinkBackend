package com.gaslink.api.modules.vendor;

import com.gaslink.api.shared.audit.AuditableEntity;
import com.gaslink.api.shared.enums.VerificationStatus;
import com.gaslink.api.shared.enums.VendorAccountStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "vendors")
public class Vendor extends AuditableEntity {
    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "business_name", nullable = false)
    private String businessName;

    @Column(name = "business_address")
    private String businessAddress;

    @Column(name = "nin", nullable = false, unique = true)
    private String nin;

    private Double lat;
    private Double lng;

    @Column(name = "service_radius_km")
    private Double serviceRadiusKm;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status")
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status")
    private VendorAccountStatus accountStatus = VendorAccountStatus.ENABLED;

    @Column(name = "account_disabled_reason")
    private String accountDisabledReason;

    @Column(name = "subscription_status")
    private String subscriptionStatus = "INACTIVE";

    @Column(name = "is_open")
    private boolean isOpen = false;

    @Column(precision = 3, scale = 2)
    private BigDecimal rating = BigDecimal.ZERO;

    @Column(name = "total_reviews")
    private int totalReviews = 0;

    public Vendor(UUID id, String businessName, String businessAddress, String nin,
                  Double lat, Double lng, Double serviceRadiusKm,
                  VerificationStatus verificationStatus, VendorAccountStatus accountStatus,
                  String accountDisabledReason, String subscriptionStatus, boolean isOpen,
                  BigDecimal rating, int totalReviews) {
        this.id = id;
        this.businessName = businessName;
        this.businessAddress = businessAddress;
        this.nin = nin;
        this.lat = lat;
        this.lng = lng;
        this.serviceRadiusKm = serviceRadiusKm;
        this.verificationStatus = verificationStatus;
        this.accountStatus = accountStatus;
        this.accountDisabledReason = accountDisabledReason;
        this.subscriptionStatus = subscriptionStatus;
        this.isOpen = isOpen;
        this.rating = rating;
        this.totalReviews = totalReviews;
    }

    // Builder pattern
    public static VendorBuilder builder() {
        return new VendorBuilder();
    }

    public static class VendorBuilder {
        private UUID id;
        private String businessName;
        private String businessAddress;
        private String nin;
        private Double lat;
        private Double lng;
        private Double serviceRadiusKm;
        private VerificationStatus verificationStatus = VerificationStatus.PENDING;
        private VendorAccountStatus accountStatus = VendorAccountStatus.ENABLED;
        private String accountDisabledReason;
        private String subscriptionStatus = "INACTIVE";
        private boolean isOpen = false;
        private BigDecimal rating = BigDecimal.ZERO;
        private int totalReviews = 0;

        public VendorBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public VendorBuilder businessName(String businessName) {
            this.businessName = businessName;
            return this;
        }

        public VendorBuilder businessAddress(String businessAddress) {
            this.businessAddress = businessAddress;
            return this;
        }

        public VendorBuilder nin(String nin) {
            this.nin = nin;
            return this;
        }

        public VendorBuilder lat(Double lat) {
            this.lat = lat;
            return this;
        }

        public VendorBuilder lng(Double lng) {
            this.lng = lng;
            return this;
        }

        public VendorBuilder serviceRadiusKm(Double serviceRadiusKm) {
            this.serviceRadiusKm = serviceRadiusKm;
            return this;
        }

        public VendorBuilder verificationStatus(VerificationStatus verificationStatus) {
            this.verificationStatus = verificationStatus;
            return this;
        }

        public VendorBuilder accountStatus(VendorAccountStatus accountStatus) {
            this.accountStatus = accountStatus;
            return this;
        }

        public VendorBuilder accountDisabledReason(String accountDisabledReason) {
            this.accountDisabledReason = accountDisabledReason;
            return this;
        }

        public VendorBuilder subscriptionStatus(String subscriptionStatus) {
            this.subscriptionStatus = subscriptionStatus;
            return this;
        }

        public VendorBuilder isOpen(boolean isOpen) {
            this.isOpen = isOpen;
            return this;
        }

        public VendorBuilder rating(BigDecimal rating) {
            this.rating = rating;
            return this;
        }

        public VendorBuilder totalReviews(int totalReviews) {
            this.totalReviews = totalReviews;
            return this;
        }

        public Vendor build() {
            return new Vendor(id, businessName, businessAddress, nin, lat, lng,
                    serviceRadiusKm, verificationStatus, accountStatus,
                    accountDisabledReason, subscriptionStatus, isOpen,
                    rating, totalReviews);
        }
    }

    // Getters and Setters (you already have these)
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getBusinessAddress() {
        return businessAddress;
    }

    public void setBusinessAddress(String businessAddress) {
        this.businessAddress = businessAddress;
    }

    public String getNin() {
        return nin;
    }

    public void setNin(String nin) {
        this.nin = nin;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public Double getServiceRadiusKm() {
        return serviceRadiusKm;
    }

    public void setServiceRadiusKm(Double serviceRadiusKm) {
        this.serviceRadiusKm = serviceRadiusKm;
    }

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(VerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public VendorAccountStatus getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(VendorAccountStatus accountStatus) {
        this.accountStatus = accountStatus;
    }

    public String getAccountDisabledReason() {
        return accountDisabledReason;
    }

    public void setAccountDisabledReason(String accountDisabledReason) {
        this.accountDisabledReason = accountDisabledReason;
    }

    public String getSubscriptionStatus() {
        return subscriptionStatus;
    }

    public void setSubscriptionStatus(String subscriptionStatus) {
        this.subscriptionStatus = subscriptionStatus;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public void setRating(BigDecimal rating) {
        this.rating = rating;
    }

    public int getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(int totalReviews) {
        this.totalReviews = totalReviews;
    }

    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Vendor vendor = (Vendor) o;
        return id != null ? id.equals(vendor.id) : vendor.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Vendor{" +
                "id=" + id +
                ", businessName='" + businessName + '\'' +
                ", businessAddress='" + businessAddress + '\'' +
                ", verificationStatus=" + verificationStatus +
                ", accountStatus=" + accountStatus +
                ", isOpen=" + isOpen +
                ", rating=" + rating +
                ", totalReviews=" + totalReviews +
                '}';
    }
}