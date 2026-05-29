package com.springboot.employeeperformance.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "review_assignments",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_assignment",
                columnNames = {"employee_id", "reviewer_id", "cycle_id"}
        ),
        indexes = {
                @Index(name = "idx_assignments_employee", columnList = "employee_id"),
                @Index(name = "idx_assignments_reviewer", columnList = "reviewer_id"),
                @Index(name = "idx_assignments_cycle",    columnList = "cycle_id")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReviewAssignment extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private Employee reviewer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cycle_id", nullable = false)
    private ReviewCycle cycle;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_type", nullable = false)
    private PerformanceReview.ReviewType reviewType;
}