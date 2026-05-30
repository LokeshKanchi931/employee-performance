package com.springboot.employeeperformance.service;

import com.springboot.employeeperformance.dto.Requests;
import com.springboot.employeeperformance.dto.Responses;
import com.springboot.employeeperformance.entity.*;
import com.springboot.employeeperformance.exception.ResourceNotFoundException;
import com.springboot.employeeperformance.mapper.EmployeeMapper;
import com.springboot.employeeperformance.mapper.ReviewMapper;
import com.springboot.employeeperformance.repository.*;
import com.springboot.employeeperformance.service.interfaces.IEmployeeService;
import com.springboot.employeeperformance.service.interfaces.IReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService implements IReviewService {

    private final PerformanceReviewRepository reviewRepository;
    private final ReviewCycleRepository       cycleRepository;
    private final GoalRepository              goalRepository;
    private final IEmployeeService            employeeService;
    private final ReviewMapper                reviewMapper;
    private final EmployeeMapper              employeeMapper;

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "employee-reviews", key = "#request.employeeId"),
            @CacheEvict(value = "cycle-summaries", key = "#request.cycleId"),
            @CacheEvict(value = "employee-ratings", allEntries = true)
    })
    public Responses.ReviewWithCycle submitReview(Requests.SubmitReview request) {
        Employee employee = employeeService.getOrThrow(request.getEmployeeId());

        ReviewCycle cycle = cycleRepository.findById(request.getCycleId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cycle not found: " + request.getCycleId()));

        if (cycle.getStatus() == ReviewCycle.Status.closed) {
            throw new IllegalArgumentException(
                    "Cannot submit a review for a closed cycle: " + cycle.getName());
        }

        Employee reviewer = null;
        if (request.getReviewerId() != null) {
            reviewer = employeeService.getOrThrow(request.getReviewerId());
        }

        PerformanceReview review = PerformanceReview.builder()
                .employee(employee)
                .cycle(cycle)
                .reviewer(reviewer)
                .reviewType(request.getReviewType() != null
                        ? request.getReviewType()
                        : PerformanceReview.ReviewType.manager)
                .rating(request.getRating())
                .reviewerNotes(request.getReviewerNotes())
                .createdBy(request.getCreatedBy() != null ? request.getCreatedBy() : "system")
                .build();

        return reviewMapper.toReviewWithCycle(reviewRepository.save(review));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "employee-reviews", key = "#employeeId")
    public Responses.PagedResponse<Responses.ReviewWithCycle> getReviewsForEmployee(Long employeeId
            , Pageable pageable) {
        employeeService.getOrThrow(employeeId);
        Page<Responses.ReviewWithCycle> page = reviewRepository.
                findByEmployeeIdWithCycleAndReviewer(employeeId, pageable)
                .map(reviewMapper::toReviewWithCycle);
        return Responses.PagedResponse.from(page);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "cycle-summaries", key = "#cycleId")
    public Responses.CycleSummary getCycleSummary(Long cycleId) {
        ReviewCycle cycle = cycleRepository.findById(cycleId)
                .orElseThrow(() -> new ResourceNotFoundException("Cycle not found: " + cycleId));

        Double avgRating = reviewRepository.findAverageRatingByCycleId(cycleId);
        if (avgRating != null) {
            avgRating = Math.round(avgRating * 100.0) / 100.0;
        }

        List<Object[]> topRows = reviewRepository.findTopPerformerByCycleId(cycleId);
        Responses.EmployeeResponse topPerformer = topRows.isEmpty()
                ? null
                : employeeMapper.toResponse((Employee) topRows.get(0)[0]);

        long completed = 0, missed = 0;
        for (Object[] row : goalRepository.countByStatusForCycle(cycleId)) {
            Goal.Status status = (Goal.Status) row[0];
            long count = (Long) row[1];
            if (status == Goal.Status.completed) completed = count;
            else if (status == Goal.Status.missed) missed = count;
        }

        return Responses.CycleSummary.builder()
                .cycleId(cycle.getId())
                .cycleName(cycle.getName())
                .cycleStatus(cycle.getStatus().name())
                .averageRating(avgRating)
                .topPerformer(topPerformer)
                .completedGoals(completed)
                .missedGoals(missed)
                .build();
    }
}