package com.springboot.employeeperformance.mapper;

import com.springboot.employeeperformance.dto.Responses;
import com.springboot.employeeperformance.entity.Employee;
import org.springframework.stereotype.Component;

/**
 * Maps Employee entities to response DTOs.
 * Extracted from EmployeeService to respect Single Responsibility —
 * the service handles business logic, the mapper handles shape transformation.
 * Injected as a bean so ReviewService and any future callers depend on the
 * mapper interface, not on a static method inside EmployeeService.
 */
@Component
public class EmployeeMapper {

    public Responses.EmployeeResponse toResponse(Employee e) {
        return Responses.EmployeeResponse.builder()
                .id(e.getId())
                .name(e.getName())
                .department(e.getDepartment())
                .role(e.getRole())
                .joiningDate(e.getJoiningDate())
                .terminationDate(e.getTerminationDate())
                .isActive(e.getIsActive())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .createdBy(e.getCreatedBy())
                .build();
    }

    public Responses.EmployeeWithRating toResponseWithRating(Employee e, Double avgRating) {
        return Responses.EmployeeWithRating.builder()
                .id(e.getId())
                .name(e.getName())
                .department(e.getDepartment())
                .role(e.getRole())
                .averageRating(avgRating)
                .build();
    }
}