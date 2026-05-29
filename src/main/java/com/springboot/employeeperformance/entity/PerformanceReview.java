package com.springboot.employeeperformance.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "performance_reviews",
        indexes = {
                @Index(name = "idx_reviews_employee",       columnList = "employee_id"),
                @Index(name = "idx_reviews_cycle",          columnList = "cycle_id"),
                @Index(name = "idx_reviews_reviewer",       columnList = "reviewer_id"),
                @Index(name = "idx_reviews_employee_cycle", columnList = "employee_id,cycle_id")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class PerformanceReview extends Auditable {

    public enum ReviewType { manager, peer, self }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cycle_id", nullable = false)
    private ReviewCycle cycle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id")
    private Employee reviewer;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_type", nullable = false)
    @Builder.Default
    private ReviewType reviewType = ReviewType.manager;

    @Column(nullable = false)
    @Min(1) @Max(5)
    private Short rating;

    @Column(name = "reviewer_notes", columnDefinition = "TEXT")
    private String reviewerNotes;

    // submitted_at is separate from created_at — it records when the review
    // was formally submitted, which may differ from when the row was created.
    @Column(name = "submitted_at", nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    @PrePersist
    @Override
    protected void onPersist() {
        super.onPersist();
        this.submittedAt = LocalDateTime.now();
    }
}