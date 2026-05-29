package com.springboot.employeeperformance.service;

import com.springboot.employeeperformance.dto.Requests;
import com.springboot.employeeperformance.dto.Responses;
import com.springboot.employeeperformance.entity.ReviewCycle;
import com.springboot.employeeperformance.exception.ResourceNotFoundException;
import com.springboot.employeeperformance.mapper.ReviewMapper;
import com.springboot.employeeperformance.repository.GoalRepository;
import com.springboot.employeeperformance.repository.ReviewCycleRepository;
import com.springboot.employeeperformance.service.interfaces.IReviewCycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewCycleService implements IReviewCycleService {

    private final ReviewCycleRepository cycleRepository;
    private final GoalRepository        goalRepository;
    private final ReviewMapper          reviewMapper;

    @Override
    @Transactional
    @CacheEvict(value = "review-cycles", allEntries = true)
    public Responses.CycleResponse createCycle(Requests.CreateCycle request) {
        if (!request.getEndDate().isAfter(request.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }
        ReviewCycle cycle = ReviewCycle.builder()
                .name(request.getName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .createdBy(request.getCreatedBy() != null ? request.getCreatedBy() : "system")
                .build();
        return reviewMapper.toCycleResponse(cycleRepository.save(cycle));
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "review-cycles", allEntries = true),
            @CacheEvict(value = "cycle-summaries", allEntries = true),
            @CacheEvict(value = "employee-ratings", allEntries = true)
    })
    public Responses.CycleResponse closeCycle(Long cycleId) {
        ReviewCycle cycle = cycleRepository.findById(cycleId)
                .orElseThrow(() -> new ResourceNotFoundException("Cycle not found: " + cycleId));

        if (cycle.getStatus() == ReviewCycle.Status.closed) {
            throw new IllegalArgumentException("Cycle is already closed: " + cycle.getName());
        }

        List<Responses.WeightViolation> violations = new ArrayList<>();
        for (Object[] row : goalRepository.sumWeightsByEmployeeForCycle(cycleId)) {
            Long   employeeId   = (Long)   row[0];
            String employeeName = (String) row[1];
            Long   totalWeight  = (Long)   row[2];

            if (totalWeight != 100) {
                violations.add(Responses.WeightViolation.builder()
                        .employeeId(employeeId)
                        .employeeName(employeeName)
                        .totalWeight(totalWeight.intValue())
                        .build());
            }
        }

        if (!violations.isEmpty()) {
            String detail = violations.stream()
                    .map(v -> v.getEmployeeName() + " (" + v.getTotalWeight() + "%)")
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");
            throw new IllegalStateException(
                    "Cannot close cycle — goal weights do not sum to 100% for: " + detail);
        }

        cycle.setStatus(ReviewCycle.Status.closed);
        return reviewMapper.toCycleResponse(cycleRepository.save(cycle));
    }
}