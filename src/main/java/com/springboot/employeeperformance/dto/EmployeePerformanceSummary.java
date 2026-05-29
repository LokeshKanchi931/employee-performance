package com.springboot.employeeperformance.dto;

import com.springboot.employeeperformance.entity.Employee;

public interface EmployeePerformanceSummary {
    Employee getEmployee();
    Double getAvgRating();
}