package com.gaslink.api.modules.user;

import com.gaslink.api.shared.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByPhone(String phone);

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.phone = :identifier OR u.email = :identifier")
    Optional<User> findByPhoneOrEmail(@Param("identifier") String identifier);

    boolean existsByPhone(String phone);

    boolean existsByEmail(String email);

    List<User> findByRole(UserRole userRole);

    Optional<User> findFirstByRole(UserRole userRole);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") UserRole role);
}