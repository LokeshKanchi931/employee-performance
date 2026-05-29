-- ============================================================
-- HR Performance Tool - Rollback Initial Schema
-- Flyway Undo Migration: U1__init_schema.sql
-- ============================================================

-- Drop child tables first due to FK dependencies

DROP TABLE IF EXISTS review_assignments;
DROP TABLE IF EXISTS goals;
DROP TABLE IF EXISTS performance_reviews;

-- 2. Drop parent tables
DROP TABLE IF EXISTS review_cycles;
DROP TABLE IF EXISTS employees;