package com.springboot.employeeperformance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.employeeperformance.dto.Requests;
import com.springboot.employeeperformance.dto.Responses;
import com.springboot.employeeperformance.service.interfaces.IGoalService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GoalController.class)
class GoalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IGoalService goalService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /goals - Success")
    void createGoal_Success() throws Exception {
        var request = new Requests.CreateGoal(
                1L,
                10L,
                "Complete Spring Boot Testing",
                25,
                "manager_user"
        );

        var response = Responses.GoalResponse.builder()
                .id(500L)
                .title("Complete Spring Boot Testing")
                .status("PENDING")
                .weight(25)
                .createdAt(LocalDateTime.now())
                .createdBy("manager_user")
                .build();

        when(goalService.createGoal(any(Requests.CreateGoal.class))).thenReturn(response);

        mockMvc.perform(post("/goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(500L))
                .andExpect(jsonPath("$.title").value("Complete Spring Boot Testing"))
                .andExpect(jsonPath("$.weight").value(25))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("POST /goals - Validation Failure (Weight too high)")
    void createGoal_ValidationFailure() throws Exception {
        // Weight is 150 (Max is 100)
        var invalidRequest = new Requests.CreateGoal(1L, 10L, "Invalid Goal", 150, "system");

        // This should return 400 Bad Request
        mockMvc.perform(post("/goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}