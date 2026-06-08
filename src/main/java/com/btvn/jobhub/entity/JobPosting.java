package com.btvn.jobhub.entity;
import com.btvn.jobhub.entity.enumType.JobStatusEnum;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "job_postings")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class JobPosting {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // [cite: 148]

    private String title; // [cite: 149]

    @Column(columnDefinition = "TEXT")
    private String description; // [cite: 150]

    private String salaryRange; // [cite: 151]

    @Enumerated(EnumType.STRING)
    private JobStatusEnum status; // [cite: 152]

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employer_id")
    private User employer; // [cite: 143]

    @OneToMany(mappedBy = "jobPosting", cascade = CascadeType.ALL)
    private List<Application> applications; // [cite: 154]
}
