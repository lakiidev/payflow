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

    @Setter
    @Column(nullable = false)
    private boolean revoked = false;

    @CreationTimestamp
    private Instant createdAt;
}
