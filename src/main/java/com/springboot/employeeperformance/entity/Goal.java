package com.springboot.employeeperformance.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "goals",
        indexes = {
                @Index(name = "idx_goals_employee",       columnList = "employee_id"),
                @Index(name = "idx_goals_cycle",          columnList = "cycle_id"),
                @Index(name = "idx_goals_employee_cycle", columnList = "employee_id,cycle_id"),
                @Index(name = "idx_goals_status",         columnList = "status")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class Goal extends Auditable {

    public enum Status { pending, completed, missed }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cycle_id", nullable = false)
    private ReviewCycle cycle;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.pending;

    @Column(nullable = false)
    @Builder.Default
    private Integer weight = 0;
}