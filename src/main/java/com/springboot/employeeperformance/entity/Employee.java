package com.springboot.employeeperformance.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "employees",
        indexes = {
                @Index(name = "idx_employees_department", columnList = "department"),
                @Index(name = "idx_employees_active",     columnList = "is_active")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Employee extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String department;

    @Column(nullable = false)
    private String role;

    @Column(name = "joining_date", nullable = false)
    private LocalDate joiningDate;

    @Column(name = "termination_date")
    private LocalDate terminationDate;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @OneToMany(mappedBy = "employee", fetch = FetchType.LAZY)
    @Builder.Default
    private List<PerformanceReview> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "reviewer", fetch = FetchType.LAZY)
    @Builder.Default
    private List<PerformanceReview> reviewsGiven = new ArrayList<>();

    @OneToMany(mappedBy = "employee", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Goal> goals = new ArrayList<>();
}