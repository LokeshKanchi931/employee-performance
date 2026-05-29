package com.springboot.employeeperformance.service.interfaces;

import com.springboot.employeeperformance.dto.Requests;
import com.springboot.employeeperformance.dto.Responses;

public interface IGoalService {
    Responses.GoalResponse createGoal(Requests.CreateGoal request);
}