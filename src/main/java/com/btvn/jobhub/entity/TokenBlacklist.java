package com.btvn.jobhub.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
@Entity
@Table(name = "token_blacklist")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class TokenBlacklist {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // [cite: 168, 172]

    @Column(nullable = false, unique = true, length = 512)
    private String tokenString; // [cite: 170]

    private LocalDateTime revokedAt; // [cite: 171]

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // [cite: 140]
}