package com.springboot.employeeperformance.repository;

import com.springboot.employeeperformance.entity.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GoalRepository extends JpaRepository<Goal, Long> {

    /**
     * Count goals by status for a cycle — used in cycle summary.
     * Returns [Status, count].
     */
    @Query("""
        SELECT g.status, COUNT(g)
        FROM Goal g
        WHERE g.cycle.id = :cycleId
        GROUP BY g.status
        """)
    List<Object[]> countByStatusForCycle(@Param("cycleId") Long cycleId);

    /**
     * Sum of goal weights per employee for a cycle.
     * Used to validate all weights = 100 before closing a cycle.
     * Returns [employeeId, employeeName, totalWeight].
     */
    @Query("""
        SELECT g.employee.id, g.employee.name, SUM(g.weight)
        FROM Goal g
        WHERE g.cycle.id = :cycleId
        GROUP BY g.employee.id, g.employee.name
        """)
    List<Object[]> sumWeightsByEmployeeForCycle(@Param("cycleId") Long cycleId);
}