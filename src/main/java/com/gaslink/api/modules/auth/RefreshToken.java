package com.gaslink.api.modules.auth;
import com.gaslink.api.shared.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "refresh_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class RefreshToken extends AuditableEntity {
    @Id @GeneratedValue @Column(columnDefinition = "uuid") private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(nullable = false, unique = true) private String token;
    @Column(name = "expires_at", nullable = false) private Instant expiresAt;
    @Column(name = "is_revoked") private boolean isRevoked = false;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isRevoked() {
        return isRevoked;
    }

    public void setRevoked(boolean revoked) {
        isRevoked = revoked;
    }
}