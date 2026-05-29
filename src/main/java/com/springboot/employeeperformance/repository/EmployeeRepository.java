package com.springboot.employeeperformance.repository;

import com.springboot.employeeperformance.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    /**
     * Active employees only — used for all forward-looking listing endpoints.
     * Filters by department (optional) and minimum average rating.
     * Single JOIN + GROUP BY — no N+1.
     */
    @Query("""
        SELECT e, AVG(r.rating) AS avgRating
        FROM Employee e
        JOIN PerformanceReview r ON r.employee = e
        WHERE e.isActive = true
          AND (:department IS NULL OR e.department = :department)
        GROUP BY e
        HAVING AVG(r.rating) >= :minRating
        ORDER BY avgRating DESC
        """)
    List<Object[]> findActiveByDepartmentAndMinRating(
            @Param("department") String department,
            @Param("minRating") double minRating);
}