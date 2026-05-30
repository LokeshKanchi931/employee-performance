package com.springboot.employeeperformance.service.interfaces;

import com.springboot.employeeperformance.dto.Requests;
import com.springboot.employeeperformance.dto.Responses;
import org.springframework.data.domain.Pageable;

public interface IReviewService {
    Responses.ReviewWithCycle submitReview(Requests.SubmitReview request);
    Responses.PagedResponse<Responses.ReviewWithCycle> getReviewsForEmployee(
            Long employeeId, Pageable pageable);
    Responses.CycleSummary getCycleSummary(Long cycleId);
}