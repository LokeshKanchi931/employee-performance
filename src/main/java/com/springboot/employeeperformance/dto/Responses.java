package com.springboot.employeeperformance.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Responses {

    @Getter @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmployeeResponse {
        private Long id;
        private String name;
        private String department;
        private String role;
        private LocalDate joiningDate;
        private LocalDate terminationDate;
        private Boolean isActive;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private String createdBy;
    }

    @Getter @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewWithCycle {
        private Long reviewId;
        private Short rating;
        private String reviewType;
        private String reviewerNotes;
        private ReviewerInfo reviewer;
        private LocalDateTime submittedAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private String createdBy;
        private CycleSummaryLine cycle;
    }

    @Getter @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewerInfo {
        private Long id;
        private String name;
    }

    @Getter @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CycleSummaryLine {
        private Long id;
        private String name;
        private LocalDate startDate;
        private LocalDate endDate;
        private String status;
    }

    @Getter @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CycleSummary {
        private Long cycleId;
        private String cycleName;
        private String cycleStatus;
        private Double averageRating;
        private EmployeeResponse topPerformer;
        private Long completedGoals;
        private Long missedGoals;
    }

    @Getter @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmployeeWithRating {
        private Long id;
        private String name;
        private String department;
        private String role;
        private Double averageRating;
    }

    @Getter @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GoalResponse {
        private Long id;
        private String title;
        private String status;
        private Integer weight;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private String createdBy;
    }

    @Getter @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CycleResponse {
        private Long id;
        private String name;
        private LocalDate startDate;
        private LocalDate endDate;
        private String status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private String createdBy;
    }

    @Getter @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeightViolation {
        private Long employeeId;
        private String employeeName;
        private Integer totalWeight;
    }
}
