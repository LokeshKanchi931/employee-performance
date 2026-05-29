package com.springboot.employeeperformance.mapper;

import com.springboot.employeeperformance.dto.Responses;
import com.springboot.employeeperformance.entity.PerformanceReview;
import com.springboot.employeeperformance.entity.ReviewCycle;
import org.springframework.stereotype.Component;

/**
 * Maps PerformanceReview and ReviewCycle entities to response DTOs.
 */
@Component
public class ReviewMapper {

    public Responses.ReviewWithCycle toReviewWithCycle(PerformanceReview r) {
        ReviewCycle c = r.getCycle();

        Responses.ReviewerInfo reviewerInfo = null;
        if (r.getReviewer() != null) {
            reviewerInfo = Responses.ReviewerInfo.builder()
                    .id(r.getReviewer().getId())
                    .name(r.getReviewer().getName())
                    .build();
        }

        return Responses.ReviewWithCycle.builder()
                .reviewId(r.getId())
                .rating(r.getRating())
                .reviewType(r.getReviewType().name())
                .reviewerNotes(r.getReviewerNotes())
                .reviewer(reviewerInfo)
                .submittedAt(r.getSubmittedAt())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .createdBy(r.getCreatedBy())
                .cycle(toCycleSummaryLine(c))
                .build();
    }

    public Responses.CycleSummaryLine toCycleSummaryLine(ReviewCycle c) {
        return Responses.CycleSummaryLine.builder()
                .id(c.getId())
                .name(c.getName())
                .startDate(c.getStartDate())
                .endDate(c.getEndDate())
                .status(c.getStatus().name())
                .build();
    }

    public Responses.CycleResponse toCycleResponse(ReviewCycle c) {
        return Responses.CycleResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .startDate(c.getStartDate())
                .endDate(c.getEndDate())
                .status(c.getStatus().name())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .createdBy(c.getCreatedBy())
                .build();
    }
}