package com.springboot.employeeperformance.service;

import com.springboot.employeeperformance.dto.Requests;
import com.springboot.employeeperformance.dto.Responses;
import com.springboot.employeeperformance.entity.*;
import com.springboot.employeeperformance.exception.ResourceNotFoundException;
import com.springboot.employeeperformance.mapper.GoalMapper;
import com.springboot.employeeperformance.repository.GoalRepository;
import com.springboot.employeeperformance.repository.ReviewCycleRepository;
import com.springboot.employeeperformance.service.interfaces.IEmployeeService;
import com.springboot.employeeperformance.service.interfaces.IGoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GoalService implements IGoalService {

    private final GoalRepository        goalRepository;
    private final ReviewCycleRepository cycleRepository;
    private final IEmployeeService      employeeService;
    private final GoalMapper            goalMapper;

    @Override
    @Transactional
    public Responses.GoalResponse createGoal(Requests.CreateGoal request) {
        Employee employee = employeeService.getOrThrow(request.getEmployeeId());

        ReviewCycle cycle = cycleRepository.findById(request.getCycleId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cycle not found: " + request.getCycleId()));

        if (cycle.getStatus() == ReviewCycle.Status.closed) {
            throw new IllegalArgumentException(
                    "Cannot add goals to a closed cycle: " + cycle.getName());
        }

        goalRepository.sumWeightsByEmployeeForCycle(request.getCycleId())
                .stream()
                .filter(row -> row[0].equals(request.getEmployeeId()))
                .findFirst()
                .ifPresent(row -> {
                    long currentTotal = (Long) row[2];
                    if (currentTotal + request.getWeight() > 100) {
                        throw new IllegalArgumentException(
                                "Adding this goal would push total weight to "
                                        + (currentTotal + request.getWeight())
                                        + "%. Max is 100%.");
                    }
                });

        Goal goal = Goal.builder()
                .employee(employee)
                .cycle(cycle)
                .title(request.getTitle())
                .weight(request.getWeight())
                .createdBy(request.getCreatedBy() != null ? request.getCreatedBy() : "system")
                .build();

        return goalMapper.toResponse(goalRepository.save(goal));
    }
}