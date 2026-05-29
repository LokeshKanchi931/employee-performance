package com.springboot.employeeperformance.service.interfaces;

import com.springboot.employeeperformance.dto.Requests;
import com.springboot.employeeperformance.dto.Responses;
import com.springboot.employeeperformance.entity.Employee;

import java.util.List;

public interface IEmployeeService {
    Responses.EmployeeResponse createEmployee(Requests.CreateEmployee request);
    Responses.EmployeeResponse terminateEmployee(Long id, Requests.TerminateEmployee request);
    List<Responses.EmployeeWithRating> findByDepartmentAndMinRating(String department, double minRating);
    Employee getOrThrow(Long id);
}