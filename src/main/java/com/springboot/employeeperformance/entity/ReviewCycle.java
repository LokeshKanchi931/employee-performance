package com.springboot.employeeperformance.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "review_cycles",
        indexes = {
                @Index(name = "idx_cycles_status", columnList = "status")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReviewCycle extends Auditable {

    public enum Status { open, closed }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.open;

    @OneToMany(mappedBy = "cycle", fetch = FetchType.LAZY)
    @Builder.Default
    private List<PerformanceReview> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "cycle", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Goal> goals = new ArrayList<>();
}