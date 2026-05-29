package com.springboot.employeeperformance.mapper;

import com.springboot.employeeperformance.dto.Responses;
import com.springboot.employeeperformance.entity.Goal;
import org.springframework.stereotype.Component;

/**
 * Maps Goal entities to response DTOs.
 */
@Component
public class GoalMapper {

    public Responses.GoalResponse toResponse(Goal g) {
        return Responses.GoalResponse.builder()
                .id(g.getId())
                .title(g.getTitle())
                .status(g.getStatus().name())
                .weight(g.getWeight())
                .createdAt(g.getCreatedAt())
                .updatedAt(g.getUpdatedAt())
                .createdBy(g.getCreatedBy())
                .build();
    }
}