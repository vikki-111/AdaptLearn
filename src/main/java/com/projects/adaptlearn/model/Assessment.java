package com.projects.adaptlearn.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.time.LocalDateTime;

@Entity
@Table(name = "assessments")
@Getter @Setter
@NoArgsConstructor
public class Assessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "overall_score")
    private BigDecimal overallScore;

    @Column(name = "passing_score")
    private Double passingScore = 60.0;

    @Column(name = "time_taken_minutes")
    private Long timeTakenMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssessmentStatus status;

    @OneToMany(mappedBy = "assessment", cascade = CascadeType.ALL)
    private List<Question> questions;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        this.completedAt = LocalDateTime.now();
        if (this.startedAt != null) {
            this.timeTakenMinutes = Duration.between(startedAt, completedAt).toMinutes();
        }
        if (this.status == null) {
            this.status = AssessmentStatus.COMPLETED;
        }
    }
}