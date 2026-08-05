package com.gaslink.api.modules.vendor.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class NearbyVendorDto {
    private UUID id;
    private String businessName;
    private Double lat;
    private Double lng;
    private BigDecimal rating;
    private int totalReviews;
    private double distanceKm;
    private BigDecimal lowestPrice;
    private boolean isOpen;

    public NearbyVendorDto(UUID id, String businessName, Double lat, Double lng,
                           BigDecimal rating, int totalReviews, double distanceKm,
                           BigDecimal lowestPrice, boolean isOpen) {
        this.id = id;
        this.businessName = businessName;
        this.lat = lat;
        this.lng = lng;
        this.rating = rating;
        this.totalReviews = totalReviews;
        this.distanceKm = distanceKm;
        this.lowestPrice = lowestPrice;
        this.isOpen = isOpen;
    }

    // Builder pattern
    public static NearbyVendorDtoBuilder builder() {
        return new NearbyVendorDtoBuilder();
    }

    public static class NearbyVendorDtoBuilder {
        private UUID id;
        private String businessName;
        private Double lat;
        private Double lng;
        private BigDecimal rating;
        private int totalReviews;
        private double distanceKm;
        private BigDecimal lowestPrice;
        private boolean isOpen;

        public NearbyVendorDtoBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public NearbyVendorDtoBuilder businessName(String businessName) {
            this.businessName = businessName;
            return this;
        }

        public NearbyVendorDtoBuilder lat(Double lat) {
            this.lat = lat;
            return this;
        }

        public NearbyVendorDtoBuilder lng(Double lng) {
            this.lng = lng;
            return this;
        }

        public NearbyVendorDtoBuilder rating(BigDecimal rating) {
            this.rating = rating;
            return this;
        }

        public NearbyVendorDtoBuilder totalReviews(int totalReviews) {
            this.totalReviews = totalReviews;
            return this;
        }

        public NearbyVendorDtoBuilder distanceKm(double distanceKm) {
            this.distanceKm = distanceKm;
            return this;
        }

        public NearbyVendorDtoBuilder lowestPrice(BigDecimal lowestPrice) {
            this.lowestPrice = lowestPrice;
            return this;
        }

        public NearbyVendorDtoBuilder isOpen(boolean isOpen) {
            this.isOpen = isOpen;
            return this;
        }

        public NearbyVendorDto build() {
            return new NearbyVendorDto(id, businessName, lat, lng, rating,
                    totalReviews, distanceKm, lowestPrice, isOpen);
        }
    }

    // Getters and Setters
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

    public double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public BigDecimal getLowestPrice() {
        return lowestPrice;
    }

    public void setLowestPrice(BigDecimal lowestPrice) {
        this.lowestPrice = lowestPrice;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    @Override
    public String toString() {
        return "NearbyVendorDto{" +
                "id=" + id +
                ", businessName='" + businessName + '\'' +
                ", lat=" + lat +
                ", lng=" + lng +
                ", rating=" + rating +
                ", totalReviews=" + totalReviews +
                ", distanceKm=" + distanceKm +
                ", lowestPrice=" + lowestPrice +
                ", isOpen=" + isOpen +
                '}';
    }
}