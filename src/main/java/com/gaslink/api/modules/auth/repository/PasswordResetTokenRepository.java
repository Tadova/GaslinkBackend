package com.gaslink.api.modules.auth.repository;

import com.gaslink.api.modules.auth.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    Optional<PasswordResetToken> findByTokenAndUsedFalse(String token);

    @Modifying
    @Transactional
    @Query("UPDATE PasswordResetToken t SET t.used = true, t.usedAt = :usedAt WHERE t.token = :token")
    void markTokenAsUsed(@Param("token") String token, @Param("usedAt") LocalDateTime usedAt);

    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordResetToken t WHERE t.expiryDate < :now OR t.used = true")
    void deleteExpiredOrUsedTokens(@Param("now") LocalDateTime now);

    void deleteByEmail(String email);

    Optional<PasswordResetToken> findByEmailAndUsedFalse(String email);
}