package com.springboot.employeeperformance.service.interfaces;

import com.springboot.employeeperformance.dto.Requests;
import com.springboot.employeeperformance.dto.Responses;
import com.springboot.employeeperformance.entity.Employee;
import org.springframework.data.domain.Pageable;

public interface IEmployeeService {
    Responses.EmployeeResponse createEmployee(Requests.CreateEmployee request);
    Responses.EmployeeResponse terminateEmployee(Long id, Requests.TerminateEmployee request);
    Responses.PagedResponse<Responses.EmployeeWithRating> findByDepartmentAndMinRating(
            String department, double minRating, Pageable pageable);
    Employee getOrThrow(Long id);
}