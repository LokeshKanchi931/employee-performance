package com.springboot.employeeperformance.controller;

import com.springboot.employeeperformance.dto.Requests;
import com.springboot.employeeperformance.dto.Responses;
import com.springboot.employeeperformance.service.interfaces.IReviewCycleService;
import com.springboot.employeeperformance.service.interfaces.IReviewService;
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
    public ResponseEntity<Responses.ReviewWithCycle> submitReview(
            @Valid @RequestBody Requests.SubmitReview request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.submitReview(request));
    }

    @GetMapping("/cycles/{id}/summary")
    public ResponseEntity<Responses.CycleSummary> getCycleSummary(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getCycleSummary(id));
    }

    @PostMapping("/cycles")
    public ResponseEntity<Responses.CycleResponse> createCycle(
            @Valid @RequestBody Requests.CreateCycle request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewCycleService.createCycle(request));
    }

    @PostMapping("/cycles/{id}/close")
    public ResponseEntity<Responses.CycleResponse> closeCycle(@PathVariable Long id) {
        return ResponseEntity.ok(reviewCycleService.closeCycle(id));
    }
}