//package com.gaslink.api.modules.address;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Modifying;
//import org.springframework.data.jpa.repository.Query;
//import java.util.*;
//
//public interface AddressRepository extends JpaRepository<Address, UUID> {
//    List<Address> findByUserId(UUID userId);
//    Optional<Address> findByUserIdAndIsDefaultTrue(UUID userId);
//    @Modifying
//    @Query("UPDATE Address a SET a.isDefault = false WHERE a.userId = :userId")
//    void clearDefaultForUser(UUID userId);
//}