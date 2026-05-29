package com.springboot.employeeperformance.service;

import com.springboot.employeeperformance.dto.Requests;
import com.springboot.employeeperformance.dto.Responses;
import com.springboot.employeeperformance.entity.Employee;
import com.springboot.employeeperformance.exception.ResourceNotFoundException;
import com.springboot.employeeperformance.mapper.EmployeeMapper;
import com.springboot.employeeperformance.repository.EmployeeRepository;
import com.springboot.employeeperformance.service.interfaces.IEmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService implements IEmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper     employeeMapper;

    @Override
    @Transactional
    @CacheEvict(value = "employee-ratings", allEntries = true)
    public Responses.EmployeeResponse createEmployee(Requests.CreateEmployee request) {
        Employee employee = Employee.builder()
                .name(request.getName())
                .department(request.getDepartment())
                .role(request.getRole())
                .joiningDate(request.getJoiningDate())
                .createdBy(request.getCreatedBy() != null ? request.getCreatedBy() : "system")
                .build();
        return employeeMapper.toResponse(employeeRepository.save(employee));
    }

    @Override
    @Transactional
    @CacheEvict(value = "employee-ratings", allEntries = true)
    public Responses.EmployeeResponse terminateEmployee(Long id, Requests.TerminateEmployee request) {
        Employee employee = getOrThrow(id);

        if (!employee.getIsActive()) {
            throw new IllegalArgumentException("Employee " + id + " is already terminated");
        }
        if (request.getTerminationDate().isBefore(employee.getJoiningDate())) {
            throw new IllegalArgumentException("Termination date cannot be before joining date");
        }

        employee.setTerminationDate(request.getTerminationDate());
        employee.setIsActive(false);
        return employeeMapper.toResponse(employeeRepository.save(employee));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "employee-ratings", key = "#department + '-' + #minRating")
    public List<Responses.EmployeeWithRating> findByDepartmentAndMinRating(
            String department, double minRating) {

        return employeeRepository
                .findActiveByDepartmentAndMinRating(department, minRating)
                .stream()
                .map(summary -> employeeMapper.toResponseWithRating
                        (summary.getEmployee(), summary.getAvgRating()))
                .toList();
    }

    @Override
    public Employee getOrThrow(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + id));
    }
}