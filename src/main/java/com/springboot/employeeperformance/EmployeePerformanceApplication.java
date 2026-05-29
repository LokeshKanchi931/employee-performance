package com.springboot.employeeperformance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class EmployeePerformanceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmployeePerformanceApplication.class, args);
    }

}
