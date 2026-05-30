package com.springboot.employeeperformance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.employeeperformance.service.interfaces.IEmployeeService;
import com.springboot.employeeperformance.service.interfaces.IReviewService;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;



@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IEmployeeService employeeService;

    @MockBean
    private IReviewService reviewService;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    void filterEmployees_ShouldThrowError_WhenRatingInvalid() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/employees")
                        .param("minRating", "6.0")) // Invalid rating
                .andExpect(status().isBadRequest());
    }

    @Test
    void getEmployeeReviews_ShouldHandleCustomSort() throws Exception {
        // Act
        mockMvc.perform(get("/employees/1/reviews")
                        .param("sort", "submittedAt,asc"))
                .andExpect(status().isOk());

        // Assert that the service received the correct Sort direction
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(reviewService).getReviewsForEmployee(eq(1L), captor.capture());

        assertEquals(Sort.Direction.ASC, captor.getValue().getSort().getOrderFor("submittedAt").getDirection());
    }
}