package com.fares_elsadek.Readly.entity;

import com.fares_elsadek.Readly.enums.TokenType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(nullable = false, unique = true, length = 200)
    private String token;
    @ManyToOne(optional = false,fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private User user;
    @Column(nullable = false)
    private Instant expiresAt;
    private Instant consumedAt;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TokenType type;
    public boolean isExpired() { return Instant.now().isAfter(expiresAt); }
    public boolean isConsumed() { return consumedAt != null; }

}
