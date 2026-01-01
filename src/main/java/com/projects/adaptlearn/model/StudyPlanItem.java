package com.projects.adaptlearn.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "study_plan_items")
@Getter @Setter
@NoArgsConstructor
public class StudyPlanItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private StudyPlan studyPlan;

    @ManyToOne(fetch = FetchType.EAGER) // Eager because we usually need the topic name immediately
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @Column(name = "is_completed", nullable = false)
    private boolean isCompleted = false;

    @Column(nullable = false)
    private Integer priority;

    @Column(name = "target_date")
    private LocalDate targetDate;

    @Column(name = "specific_note", columnDefinition = "TEXT")
    private String specificNote;
}