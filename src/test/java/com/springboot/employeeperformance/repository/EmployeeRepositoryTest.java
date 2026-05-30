package com.springboot.employeeperformance.repository;

import com.springboot.employeeperformance.dto.EmployeePerformanceSummary;
import com.springboot.employeeperformance.entity.Employee;
import com.springboot.employeeperformance.entity.PerformanceReview;
import com.springboot.employeeperformance.entity.ReviewCycle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private TestEntityManager entityManager;

    private ReviewCycle activeCycle;

    @BeforeEach
    void setUp() {
        // 1. Create a ReviewCycle (Required for PerformanceReview)
        activeCycle = ReviewCycle.builder()
                .name("Annual 2026")
                .startDate(LocalDate.now().minusMonths(1))
                .endDate(LocalDate.now().plusMonths(1))
                .status(ReviewCycle.Status.open)
                .createdBy("test-admin")
                .build();
        entityManager.persist(activeCycle);

        // 2. Create Employees
        Employee alice = createTestEmployee("Alice", "Engineering");
        Employee bob = createTestEmployee("Bob", "Sales");

        // 3. Create Reviews (Using the ReviewCycle created above)
        createReview(alice, (short) 4);
        createReview(alice, (short) 5); // Alice average = 4.5
        createReview(bob, (short) 3);   // Bob average = 3.0

        entityManager.flush();
        entityManager.clear(); // Clear cache to ensure we hit the DB for the query
    }

    @Test
    void findActiveByDepartmentAndMinRating_FiltersCorrectResult() {
        // Act: Find Engineering employees with at least 4.0 rating
        Page<EmployeePerformanceSummary> result = employeeRepository
                .findActiveByDepartmentAndMinRating("Engineering", 4.0, PageRequest.of(0, 10));

        // Assert
        assertThat(result.getContent()).hasSize(1);
        EmployeePerformanceSummary summary = result.getContent().get(0);
        assertThat(summary.getEmployee().getName()).isEqualTo("Alice");
        assertThat(summary.getAvgRating()).isEqualTo(4.5);
    }

    @Test
    void findActiveByDepartmentAndMinRating_ReturnsZeroForNoReviews() {
        // Arrange: Create Charlie in Engineering with NO reviews
        createTestEmployee("Charlie", "Engineering");
        entityManager.flush();

        // Act: Search with minRating 0.0 (Charlie should appear with 0.0 rating due to COALESCE)
        Page<EmployeePerformanceSummary> result = employeeRepository
                .findActiveByDepartmentAndMinRating("Engineering", 0.0, PageRequest.of(0, 10));

        // Assert
        assertThat(result.getContent()).anyMatch(s -> s.getEmployee().getName().equals("Charlie") && s.getAvgRating() == 0.0);
    }

    // --- Helper Methods to simplify setup ---

    private Employee createTestEmployee(String name, String dept) {
        Employee emp = Employee.builder()
                .name(name)
                .department(dept)
                .role("Developer")
                .isActive(true)
                .joiningDate(LocalDate.now())
                .createdBy("test-admin")
                .build();
        return entityManager.persist(emp);
    }

    private void createReview(Employee emp, short rating) {
        PerformanceReview review = PerformanceReview.builder()
                .employee(emp)
                .cycle(activeCycle) // Must not be null
                .rating(rating)
                .reviewType(PerformanceReview.ReviewType.manager)
                .createdBy("test-admin")
                .build();
        entityManager.persist(review);
    }
}