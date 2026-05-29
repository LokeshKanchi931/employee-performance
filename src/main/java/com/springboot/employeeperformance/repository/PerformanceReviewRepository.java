package com.springboot.employeeperformance.repository;

import com.springboot.employeeperformance.entity.PerformanceReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, Long> {

    /**
     * All reviews for an employee, with cycle and reviewer eagerly loaded.
     * JOIN FETCH avoids N+1 for both associations.
     */
    @Query("""
        SELECT r FROM PerformanceReview r
        JOIN FETCH r.cycle
        LEFT JOIN FETCH r.reviewer
        WHERE r.employee.id = :employeeId
        ORDER BY r.submittedAt DESC
        """)
    List<PerformanceReview> findByEmployeeIdWithCycleAndReviewer(@Param("employeeId") Long employeeId);

    /**
     * Average rating for a cycle.
     * Includes terminated employees only if their review was submitted before termination.
     * Active employees are always included.
     */
    @Query("""
        SELECT AVG(r.rating)
        FROM PerformanceReview r
        WHERE r.cycle.id = :cycleId
          AND r.reviewType = com.springboot.employeeperformance.entity.PerformanceReview$ReviewType.manager
          AND (
              r.employee.isActive = true
              OR (r.employee.terminationDate IS NOT NULL
                  AND r.submittedAt < CAST(r.employee.terminationDate AS java.time.LocalDateTime))
          )
        """)
    Double findAverageRatingByCycleId(@Param("cycleId") Long cycleId);

    /**
     * Top performer in a cycle (highest average rating).
     * Applies the same terminated-employee inclusion rule.
     * Returns [Employee, avgRating] ordered desc — first row is the top performer.
     */
    @Query("""
        SELECT r.employee, AVG(r.rating) AS avg
        FROM PerformanceReview r
        WHERE r.cycle.id = :cycleId
          AND r.reviewType = com.springboot.employeeperformance.entity.PerformanceReview$ReviewType.manager
          AND (
              r.employee.isActive = true
              OR (r.employee.terminationDate IS NOT NULL
                  AND r.submittedAt < CAST(r.employee.terminationDate AS java.time.LocalDateTime))
          )
        GROUP BY r.employee
        ORDER BY avg DESC
        """)
    List<Object[]> findTopPerformerByCycleId(@Param("cycleId") Long cycleId);
}