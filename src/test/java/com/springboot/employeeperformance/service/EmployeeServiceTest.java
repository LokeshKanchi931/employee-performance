package com.springboot.employeeperformance.service;

import com.springboot.employeeperformance.dto.EmployeePerformanceSummary;
import com.springboot.employeeperformance.dto.Requests;
import com.springboot.employeeperformance.dto.Responses;
import com.springboot.employeeperformance.entity.Employee;
import com.springboot.employeeperformance.exception.ResourceNotFoundException;
import com.springboot.employeeperformance.mapper.EmployeeMapper;
import com.springboot.employeeperformance.repository.EmployeeRepository;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock EmployeeRepository employeeRepository;
    @Mock EmployeeMapper     employeeMapper;
    @InjectMocks EmployeeService employeeService;

    private Employee alice;
    private Responses.EmployeeResponse aliceResponse;
    private Pageable defaultPageable;

    @BeforeEach
    void setUp() {
        alice = Employee.builder()
                .id(1L).name("Alice").department("Engineering")
                .role("SWE").joiningDate(LocalDate.of(2022, 1, 10))
                .isActive(true)
                .build();

        aliceResponse = Responses.EmployeeResponse.builder()
                .id(1L).name("Alice").department("Engineering")
                .role("SWE").joiningDate(LocalDate.of(2022, 1, 10))
                .isActive(true)
                .build();

        defaultPageable = PageRequest.of(0, 20);
    }

    //Test Create Employee

    @Nested
    @DisplayName("createEmployee")
    class CreateEmployee {

        @Test
        @DisplayName("saves employee and returns mapped DTO")
        void savesAndReturnsDto() {
            var request = new Requests.CreateEmployee(
                    "Alice", "Engineering", "SWE",
                    LocalDate.of(2022, 1, 10), "system");

            when(employeeRepository.save(any())).thenReturn(alice);
            when(employeeMapper.toResponse(alice)).thenReturn(aliceResponse);

            Responses.EmployeeResponse result = employeeService.createEmployee(request);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("Alice");
            verify(employeeRepository).save(any(Employee.class));
        }

        @Test
        @DisplayName("sets createdBy to 'system' when not provided")
        void defaultsCreatedByToSystem() {
            var request = new Requests.CreateEmployee(
                    "Alice", "Engineering", "SWE",
                    LocalDate.of(2022, 1, 10), null);

            when(employeeRepository.save(any())).thenReturn(alice);
            when(employeeMapper.toResponse(any())).thenReturn(aliceResponse);

            employeeService.createEmployee(request);

            verify(employeeRepository).save(argThat(e -> "system".equals(e.getCreatedBy())));
        }
    }

    // Terminate Employee

    @Nested
    @DisplayName("terminateEmployee")
    class TerminateEmployee {

        @Test
        @DisplayName("sets isActive=false and terminationDate")
        void setsIsActiveToFalse() {
            var request = new Requests.TerminateEmployee(LocalDate.of(2025, 4, 1));
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(alice));
            when(employeeRepository.save(any())).thenReturn(alice);
            when(employeeMapper.toResponse(any())).thenReturn(aliceResponse);

            employeeService.terminateEmployee(1L, request);

            verify(employeeRepository).save(argThat(e ->
                    !e.getIsActive() &&
                            e.getTerminationDate().equals(LocalDate.of(2025, 4, 1))));
        }

        @Test
        @DisplayName("throws when employee is already terminated")
        void throwsWhenAlreadyTerminated() {
            alice.setIsActive(false);
            alice.setTerminationDate(LocalDate.of(2025, 1, 1));
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(alice));

            assertThatThrownBy(() ->
                    employeeService.terminateEmployee(1L,
                            new Requests.TerminateEmployee(LocalDate.of(2025, 6, 1))))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("already terminated");
        }

        @Test
        @DisplayName("throws when termination date is before joining date")
        void throwsWhenDateBeforeJoining() {
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(alice));

            assertThatThrownBy(() ->
                    employeeService.terminateEmployee(1L,
                            new Requests.TerminateEmployee(LocalDate.of(2020, 1, 1))))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("before joining date");
        }
    }


    @Nested
    @DisplayName("getOrThrow")
    class GetOrThrow {

        @Test
        @DisplayName("returns employee when found")
        void returnsEmployeeWhenFound() {
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(alice));
            assertThat(employeeService.getOrThrow(1L)).isEqualTo(alice);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when not found")
        void throwsWhenNotFound() {
            when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeService.getOrThrow(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    // findByDepartmentAndMinRating

    @Nested
    @DisplayName("findByDepartmentAndMinRating")
    class FilterEmployees {

        @Test
        @DisplayName("returns paged response with mapped content")
        void returnsMappedPagedResponse() {
            EmployeePerformanceSummary summary = mockSummary(alice, 4.5);
            var pageResult = new PageImpl<>(List.of(summary), defaultPageable, 1);
            var expectedDto = Responses.EmployeeWithRating.builder()
                    .id(1L).name("Alice").averageRating(4.5).build();

            when(employeeRepository.findActiveByDepartmentAndMinRating(
                    eq("Engineering"), eq(3.0), eq(defaultPageable)))
                    .thenReturn(pageResult);
            when(employeeMapper.toResponseWithRating(any(Employee.class), anyDouble())).thenReturn(expectedDto);

            Responses.PagedResponse<Responses.EmployeeWithRating> result =
                    employeeService.findByDepartmentAndMinRating("Engineering", 3.0, defaultPageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getAverageRating()).isEqualTo(4.5);
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getTotalPages()).isEqualTo(1);
            assertThat(result.isLast()).isTrue();
        }

        @Test
        @DisplayName("returns empty paged response when no matches")
        void returnsEmptyPagedResponse() {
            var emptyPage = new PageImpl<EmployeePerformanceSummary>(
                    List.of(), defaultPageable, 0);

            when(employeeRepository.findActiveByDepartmentAndMinRating(
                    any(), anyDouble(), any()))
                    .thenReturn(emptyPage);

            Responses.PagedResponse<Responses.EmployeeWithRating> result =
                    employeeService.findByDepartmentAndMinRating("HR", 5.0, defaultPageable);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }

        @Test
        @DisplayName("page metadata is correctly populated")
        void pageMetadataIsCorrect() {
            EmployeePerformanceSummary s1 = mockSummary(alice, 4.5);
            Pageable pageable = PageRequest.of(1, 2);
            var pageResult = new PageImpl<>(List.of(s1), pageable, 5);

            when(employeeRepository.findActiveByDepartmentAndMinRating(any(), anyDouble(), any()))
                    .thenReturn(pageResult);
            when(employeeMapper.toResponseWithRating(any(Employee.class),anyDouble()))
                    .thenReturn(Responses.EmployeeWithRating.builder()
                            .id(1L).name("Alice").averageRating(4.5).build());

            Responses.PagedResponse<Responses.EmployeeWithRating> result =
                    employeeService.findByDepartmentAndMinRating(null, 0, pageable);

            assertThat(result.getPage()).isEqualTo(1);
            assertThat(result.getSize()).isEqualTo(2);
            assertThat(result.getTotalElements()).isEqualTo(5);
            assertThat(result.getTotalPages()).isEqualTo(3);
            assertThat(result.isLast()).isFalse();
        }
    }


    private EmployeePerformanceSummary mockSummary(Employee employee, double avgRating) {
        return new EmployeePerformanceSummary() {
            @Override public Employee getEmployee() { return employee; }
            @Override public Double getAvgRating()  { return avgRating; }
        };
    }
}