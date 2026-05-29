package com.springboot.employeeperformance.controller;

import com.springboot.employeeperformance.dto.Requests;
import com.springboot.employeeperformance.dto.Responses;
import com.springboot.employeeperformance.service.interfaces.IGoalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/goals")
@RequiredArgsConstructor
public class GoalController {

    private final IGoalService goalService;

    @PostMapping
    @Operation(
            summary = "Create a new performance goal",
            description = "Assigns a new measurable target or objective to an employee. The request is validated to ensure proper dates and metrics are provided.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Goal successfully created and assigned"),
                    @ApiResponse(responseCode = "400", description = "Invalid request payload (e.g., missing fields, weight exceeds limit, or past deadlines)"),
                    @ApiResponse(responseCode = "404", description = "Assigned Employee or Review Cycle not found")
            }
    )
    public ResponseEntity<Responses.GoalResponse> createGoal(
            @Valid @RequestBody Requests.CreateGoal request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(goalService.createGoal(request));
    }
}