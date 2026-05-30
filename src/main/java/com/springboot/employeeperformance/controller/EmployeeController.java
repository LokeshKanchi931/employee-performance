package com.springboot.employeeperformance.controller;

import com.springboot.employeeperformance.dto.Requests;
import com.springboot.employeeperformance.dto.Responses;
import com.springboot.employeeperformance.service.interfaces.IEmployeeService;
import com.springboot.employeeperformance.service.interfaces.IReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final IEmployeeService employeeService;
    private final IReviewService   reviewService;

    @PostMapping
    @Operation(
            summary = "Onboard a new employee",
            description = "Creates a new employee record in the system with the provided personal and department details.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Employee created successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request body supplied")
            }
    )
    public ResponseEntity<Responses.EmployeeResponse> createEmployee(
            @Valid @RequestBody Requests.CreateEmployee request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(employeeService.createEmployee(request));
    }

    @PatchMapping("/{id}/terminate")
    @Operation(
            summary = "Terminate an employee",
            description = "Updates an employee's status to terminated and records the termination date and reason.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Employee terminated successfully"),
                    @ApiResponse(responseCode = "404", description = "Employee ID not found")
            }
    )
    public ResponseEntity<Responses.EmployeeResponse> terminateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody Requests.TerminateEmployee request) {
        return ResponseEntity.ok(employeeService.terminateEmployee(id, request));
    }

    @GetMapping("/{id}/reviews")
    @Operation(
            summary = "Get employee review history",
            description = "Retrieves a list of all performance reviews associated with a specific employee, including the review cycle data.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved reviews"),
                    @ApiResponse(responseCode = "404", description = "Employee ID not found")
            }
    )
    public ResponseEntity<Responses.PagedResponse<Responses.ReviewWithCycle>> getEmployeeReviews(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "submittedAt,desc") String sort) {

        Pageable pageable = buildPageable(page, size, sort);
        return ResponseEntity.ok(reviewService.getReviewsForEmployee(id,pageable));
    }

    @GetMapping
    @Operation(
            summary = "Filter employees",
            description = "Query employees based on their department and a minimum performance rating threshold.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully filtered employee list"),
                    @ApiResponse(responseCode = "400", description = "Invalid rating range provided (must be 0-5)")
            }
    )
    public ResponseEntity<Responses.PagedResponse<Responses.EmployeeWithRating>> filterEmployees(
            @RequestParam(required = false) String department,
            @RequestParam(defaultValue = "0") double minRating,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        if (minRating < 0 || minRating > 5) {
            throw new IllegalArgumentException("minRating must be between 0 and 5");
        }
        if (size > 100) {
            throw new IllegalArgumentException("Page size must not exceed 100");
        }
        Pageable pageable = PageRequest.of(page, size);

        return ResponseEntity.ok(
                employeeService.findByDepartmentAndMinRating(department, minRating, pageable));
    }

    private Pageable buildPageable(int page, int size, String sort) {
        if (size > 100) {
            throw new IllegalArgumentException("Page size must not exceed 100");
        }
        // sort format: "field,direction" e.g. "submittedAt,desc"
        String[] parts = sort.split(",");
        String field = parts[0];
        Sort.Direction direction = parts.length > 1 && parts[1].equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        return PageRequest.of(page, size, Sort.by(direction, field));
    }
}