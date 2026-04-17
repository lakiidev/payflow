package com.payflow.domain.model.token;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Table(name = "refresh_tokens")
@Entity
@NoArgsConstructor
@Builder
@Getter
@AllArgsConstructor
public class RefreshToken {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable=false)
    private UUID userId;

    @Column(nullable=false, unique = true)
    private String tokenHash;

    @Column(nullable=false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean revoked = false;

    @CreationTimestamp
    private Instant createdAt;

    public void revoke() {
        if (this.revoked) {
            throw new InvalidRefreshTokenException("Token already revoked");
        }
        this.revoked = true;
    }

    public boolean isExpired() {
        return expiresAt.isBefore(Instant.now());
    }

    public void validate() {
        if (this.revoked) throw new InvalidRefreshTokenException("Token already revoked");
        if (isExpired()) throw new InvalidRefreshTokenException("Token expired");
    }
}
