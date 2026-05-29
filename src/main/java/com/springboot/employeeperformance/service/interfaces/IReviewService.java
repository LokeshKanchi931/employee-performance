package com.springboot.employeeperformance.service.interfaces;

import com.springboot.employeeperformance.dto.Requests;
import com.springboot.employeeperformance.dto.Responses;

import java.util.List;

public interface IReviewService {
    Responses.ReviewWithCycle submitReview(Requests.SubmitReview request);
    List<Responses.ReviewWithCycle> getReviewsForEmployee(Long employeeId);
    Responses.CycleSummary getCycleSummary(Long cycleId);
}