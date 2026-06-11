package com.btvn.jobhub.entity;
import com.btvn.jobhub.entity.enumType.JobStatusEnum;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "job_postings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobPosting {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String salaryRange;

    @Enumerated(EnumType.STRING)
    private JobStatusEnum status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employer_id")
    private User employer;

    @OneToMany(mappedBy = "jobPosting", cascade = CascadeType.ALL)
    private List<Application> applications;
}
