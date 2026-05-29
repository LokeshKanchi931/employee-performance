package com.springboot.employeeperformance.controller;

import com.springboot.employeeperformance.dto.Requests;
import com.springboot.employeeperformance.dto.Responses;
import com.springboot.employeeperformance.service.interfaces.IReviewCycleService;
import com.springboot.employeeperformance.service.interfaces.IReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final IReviewService      reviewService;
    private final IReviewCycleService reviewCycleService;

    @PostMapping("/reviews")
    @Operation(
            summary = "Submit a performance review",
            description = "Submits a manager or self-evaluation review for an employee within an active review cycle.",
            tags = {"Performance Reviews"},
            responses = {
                    @ApiResponse(responseCode = "201", description = "Review submitted successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request payload (e.g., scoring out of bounds)"),
                    @ApiResponse(responseCode = "409", description = "Review already exists for this employee in the specified cycle")
            }
    )
    public ResponseEntity<Responses.ReviewWithCycle> submitReview(
            @Valid @RequestBody Requests.SubmitReview request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.submitReview(request));
    }

    @GetMapping("/cycles/{id}/summary")
    @Operation(
            summary = "Get review cycle summary metrics",
            description = "Calculates and returns aggregated high-level statistics (e.g., average ratings, completion rates) for a specific cycle.",
            tags = {"Review Cycles"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Summary metrics generated successfully"),
                    @ApiResponse(responseCode = "404", description = "Review cycle ID not found")
            }
    )
    public ResponseEntity<Responses.CycleSummary> getCycleSummary(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getCycleSummary(id));
    }

    @PostMapping("/cycles")
    @Operation(
            summary = "Create a new review cycle",
            description = "Initializes a new time period (e.g., 'Q3 2026 Performance Review') where goals can be assessed and reviews submitted.",
            tags = {"Review Cycles"},
            responses = {
                    @ApiResponse(responseCode = "201", description = "Review cycle created successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid date range or overlapping cycle configuration")
            }
    )
    public ResponseEntity<Responses.CycleResponse> createCycle(
            @Valid @RequestBody Requests.CreateCycle request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewCycleService.createCycle(request));
    }

    @PostMapping("/cycles/{id}/close")
    @Operation(
            summary = "Close an active review cycle",
            description = "Finalizes all scores and locks the review cycle to prevent further changes or submissions.",
            tags = {"Review Cycles"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Review cycle closed successfully"),
                    @ApiResponse(responseCode = "404", description = "Review cycle ID not found"),
                    @ApiResponse(responseCode = "400", description = "Cycle is already closed")
            }
    )
    public ResponseEntity<Responses.CycleResponse> closeCycle(@PathVariable Long id) {
        return ResponseEntity.ok(reviewCycleService.closeCycle(id));
    }
}