package com.springboot.employeeperformance.repository;

import com.springboot.employeeperformance.entity.ReviewAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewAssignmentRepository extends JpaRepository<ReviewAssignment, Long> {

    @Query("""
        SELECT a FROM ReviewAssignment a
        JOIN FETCH a.employee
        JOIN FETCH a.reviewer
        WHERE a.cycle.id = :cycleId
        """)
    List<ReviewAssignment> findByCycleId(@Param("cycleId") Long cycleId);
}