package com.springboot.employeeperformance.controller;

import com.springboot.employeeperformance.dto.Requests;
import com.springboot.employeeperformance.dto.Responses;
import com.springboot.employeeperformance.service.interfaces.IEmployeeService;
import com.springboot.employeeperformance.service.interfaces.IReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<Responses.EmployeeResponse> createEmployee(
            @Valid @RequestBody Requests.CreateEmployee request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(employeeService.createEmployee(request));
    }

    @PatchMapping("/{id}/terminate")
    public ResponseEntity<Responses.EmployeeResponse> terminateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody Requests.TerminateEmployee request) {
        return ResponseEntity.ok(employeeService.terminateEmployee(id, request));
    }

    @GetMapping("/{id}/reviews")
    public ResponseEntity<List<Responses.ReviewWithCycle>> getEmployeeReviews(
            @PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getReviewsForEmployee(id));
    }

    @GetMapping
    public ResponseEntity<List<Responses.EmployeeWithRating>> filterEmployees(
            @RequestParam(required = false) String department,
            @RequestParam(defaultValue = "0") double minRating) {

        if (minRating < 0 || minRating > 5) {
            throw new IllegalArgumentException("minRating must be between 0 and 5");
        }
        return ResponseEntity.ok(
                employeeService.findByDepartmentAndMinRating(department, minRating));
    }
}