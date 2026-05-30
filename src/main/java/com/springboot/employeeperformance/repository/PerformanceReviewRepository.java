package com.springboot.employeeperformance.repository;

import com.springboot.employeeperformance.entity.PerformanceReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, Long> {

    /**
     * All reviews for an employee, with cycle and reviewer eagerly loaded
     */
    @Query(
            value = """
            SELECT r FROM PerformanceReview r
            JOIN FETCH r.cycle
            LEFT JOIN FETCH r.reviewer
            WHERE r.employee.id = :employeeId
            ORDER BY r.submittedAt DESC
            """,
            countQuery = """
            SELECT COUNT(r) FROM PerformanceReview r
            WHERE r.employee.id = :employeeId
            """
    )
    Page<PerformanceReview> findByEmployeeIdWithCycleAndReviewer(
            @Param("employeeId") Long employeeId,
            Pageable pageable);

    /**
     * Average rating for a cycle — manager reviews only.
     * Includes terminated employees only if review was submitted before termination.
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
     * Top performer in a cycle — highest manager-only average rating.
     * Returns [Employee, avgRating] ordered desc.
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