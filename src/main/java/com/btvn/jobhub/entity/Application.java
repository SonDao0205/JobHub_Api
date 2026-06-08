package com.btvn.jobhub.entity;
import com.btvn.jobhub.entity.enumType.ApplicationStatusEnum;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "applications")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Application {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // [cite: 173]

    @Column(columnDefinition = "TEXT")
    private String coverLetter; // [cite: 174]

    private String cvUrl; // [cite: 175]

    private LocalDateTime appliedAt; // [cite: 176]

    @Enumerated(EnumType.STRING)
    private ApplicationStatusEnum status; // [cite: 177]

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id")
    private User candidate; // [cite: 144]

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_posting_id")
    private JobPosting jobPosting; // [cite: 154]
}
