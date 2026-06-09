package com.btvn.jobhub.entity;
import com.btvn.jobhub.entity.enumType.RoleEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // [cite: 138]

    @Column(nullable = false, unique = true)
    private String email; // [cite: 138]

    @Column(nullable = false)
    private String passwordHash; // [cite: 138]

    @Enumerated(EnumType.STRING)
    private RoleEnum role; // [cite: 138]

    private Boolean isActive; // [cite: 138]

    @OneToMany(mappedBy = "employer", cascade = CascadeType.ALL)
    private List<JobPosting> jobPostings; // [cite: 143]

    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL)
    private List<Application> applications; // [cite: 144]

    private String resetToken;
    private LocalDateTime resetTokenExpiry;
}