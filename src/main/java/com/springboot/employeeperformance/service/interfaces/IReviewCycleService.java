package com.springboot.employeeperformance.service.interfaces;

import com.springboot.employeeperformance.dto.Requests;
import com.springboot.employeeperformance.dto.Responses;

public interface IReviewCycleService {
    Responses.CycleResponse createCycle(Requests.CreateCycle request);
    Responses.CycleResponse closeCycle(Long cycleId);
}