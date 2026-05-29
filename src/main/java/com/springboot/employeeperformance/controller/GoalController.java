package com.springboot.employeeperformance.controller;

import com.springboot.employeeperformance.dto.Requests;
import com.springboot.employeeperformance.dto.Responses;
import com.springboot.employeeperformance.service.interfaces.IGoalService;
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
    public ResponseEntity<Responses.GoalResponse> createGoal(
            @Valid @RequestBody Requests.CreateGoal request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(goalService.createGoal(request));
    }
}