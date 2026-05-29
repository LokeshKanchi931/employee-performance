package com.springboot.employeeperformance.repository;

import com.springboot.employeeperformance.entity.ReviewCycle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewCycleRepository extends JpaRepository<ReviewCycle, Long> {
}