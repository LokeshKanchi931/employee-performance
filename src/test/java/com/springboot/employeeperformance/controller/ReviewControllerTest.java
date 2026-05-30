package com.springboot.employeeperformance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.employeeperformance.dto.Requests;
import com.springboot.employeeperformance.dto.Responses;
import com.springboot.employeeperformance.entity.PerformanceReview.ReviewType;
import com.springboot.employeeperformance.service.interfaces.IReviewCycleService;
import com.springboot.employeeperformance.service.interfaces.IReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IReviewService reviewService;

    @MockBean
    private IReviewCycleService reviewCycleService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void submitReview_Success() throws Exception {
        // Build Request
        var request = new Requests.SubmitReview(
                1L, 10L, 5L, ReviewType.manager, (short) 4, "Great progress", "system"
        );

        // Build Response using your @Builder
        var response = Responses.ReviewWithCycle.builder()
                .reviewId(100L)
                .rating((short) 4)
                .reviewerNotes("Great progress")
                .createdAt(LocalDateTime.now())
                .build();

        when(reviewService.submitReview(any())).thenReturn(response);

        mockMvc.perform(post("/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reviewId").value(100L))
                .andExpect(jsonPath("$.rating").value(4));
    }

    @Test
    void createCycle_Success() throws Exception {
        // Build Request
        var request = new Requests.CreateCycle(
                "Annual 2026",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                "system"
        );

        // FIX: Build Response using your @Builder to match the 8 fields
        var response = Responses.CycleResponse.builder()
                .id(1L)
                .name("Annual 2026")
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 12, 31))
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .createdBy("system")
                .build();

        when(reviewCycleService.createCycle(any())).thenReturn(response);

        mockMvc.perform(post("/cycles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Annual 2026"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void getCycleSummary_Success() throws Exception {
        var summary = Responses.CycleSummary.builder()
                .cycleId(10L)
                .averageRating(4.5)
                .cycleStatus("ACTIVE")
                .build();

        when(reviewService.getCycleSummary(10L)).thenReturn(summary);

        mockMvc.perform(get("/cycles/10/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cycleId").value(10L))
                .andExpect(jsonPath("$.averageRating").value(4.5));
    }
}