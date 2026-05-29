package com.springboot.employeeperformance.dto;

import com.springboot.employeeperformance.entity.PerformanceReview.ReviewType;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

public class Requests {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class CreateEmployee {
        @NotBlank(message = "Name is required")
        private String name;
        @NotBlank(message = "Department is required")
        private String department;
        @NotBlank(message = "Role is required")
        private String role;
        @NotNull(message = "Joining date is required")
        @PastOrPresent(message = "Joining date cannot be in the future")
        private LocalDate joiningDate;
        // Who is creating this record — defaults to "system" if not provided.
        // In a real system this would come from the auth token, not the request body.
        private String createdBy = "system";
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class TerminateEmployee {
        @NotNull(message = "Termination date is required")
        private LocalDate terminationDate;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class SubmitReview {
        @NotNull(message = "Employee ID is required")
        private Long employeeId;
        @NotNull(message = "Cycle ID is required")
        private Long cycleId;
        private Long reviewerId;
        private ReviewType reviewType = ReviewType.manager;
        @NotNull(message = "Rating is required")
        @Min(value = 1, message = "Rating must be at least 1")
        @Max(value = 5, message = "Rating must be at most 5")
        private Short rating;
        private String reviewerNotes;
        private String createdBy = "system";
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class CreateCycle {
        @NotBlank(message = "Name is required")
        private String name;
        @NotNull(message = "Start date is required")
        private LocalDate startDate;
        @NotNull(message = "End date is required")
        private LocalDate endDate;
        private String createdBy = "system";
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class CreateGoal {
        @NotNull(message = "Employee ID is required")
        private Long employeeId;
        @NotNull(message = "Cycle ID is required")
        private Long cycleId;
        @NotBlank(message = "Title is required")
        private String title;
        @NotNull(message = "Weight is required")
        @Min(value = 1,   message = "Weight must be at least 1")
        @Max(value = 100, message = "Weight must be at most 100")
        private Integer weight;
        private String createdBy = "system";
    }
}