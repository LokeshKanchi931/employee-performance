package com.springboot.employeeperformance.repository;

import com.springboot.employeeperformance.dto.EmployeePerformanceSummary;
import com.springboot.employeeperformance.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    /**
     * Active employees only — used for all forward-looking listing endpoints.
     * Filters by department (optional) and minimum average rating.
     */
    @Query(
            value = """
            SELECT e AS employee,
                   COALESCE(AVG(r.rating), 0.0) AS avgRating
            FROM Employee e
            LEFT JOIN PerformanceReview r ON r.employee = e
            WHERE e.isActive = true
              AND (:department IS NULL OR e.department = :department)
            GROUP BY e
            HAVING COALESCE(AVG(r.rating), 0.0) >= :minRating
            ORDER BY avgRating DESC
            """,
            countQuery = """
            SELECT COUNT(DISTINCT e)
            FROM Employee e
            LEFT JOIN PerformanceReview r ON r.employee = e
            WHERE e.isActive = true
              AND (:department IS NULL OR e.department = :department)
            GROUP BY e
            HAVING COALESCE(AVG(r.rating), 0.0) >= :minRating
            """
    )
    Page<EmployeePerformanceSummary> findActiveByDepartmentAndMinRating(
            @Param("department") String department,
            @Param("minRating") double minRating,
            Pageable pageable);
}