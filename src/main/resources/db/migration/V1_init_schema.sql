-- ============================================================
-- HR Performance Tool - Complete Initial Schema
-- ============================================================

-- ── EMPLOYEES ────────────────────────────────────────────────
CREATE TABLE employees (
                           id                  BIGSERIAL PRIMARY KEY,
                           name                VARCHAR(255)    NOT NULL,
                           department          VARCHAR(100)    NOT NULL,
                           role                VARCHAR(100)    NOT NULL,
                           joining_date        DATE            NOT NULL,
                           termination_date    DATE            NULL,
                           is_active           BOOLEAN         NOT NULL DEFAULT TRUE,
                           created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
                           updated_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
                           created_by          VARCHAR(255)    NOT NULL DEFAULT 'system',

                           CONSTRAINT chk_name_not_empty        CHECK (TRIM(name) <> ''),
                           CONSTRAINT chk_department_not_empty  CHECK (TRIM(department) <> ''),
                           CONSTRAINT chk_termination_after_joining
                               CHECK (termination_date IS NULL OR termination_date > joining_date)
);

CREATE INDEX idx_employees_department ON employees(department);
CREATE INDEX idx_employees_active     ON employees(is_active);


-- ── REVIEW CYCLES ────────────────────────────────────────────
CREATE TABLE review_cycles (
                               id          BIGSERIAL PRIMARY KEY,
                               name        VARCHAR(100)    NOT NULL UNIQUE,
                               start_date  DATE            NOT NULL,
                               end_date    DATE            NOT NULL,
                               status      VARCHAR(20)     NOT NULL DEFAULT 'open',
                               created_at  TIMESTAMP       NOT NULL DEFAULT NOW(),
                               updated_at  TIMESTAMP       NOT NULL DEFAULT NOW(),
                               created_by  VARCHAR(255)    NOT NULL DEFAULT 'system',

                               CONSTRAINT chk_cycle_dates  CHECK (end_date > start_date),
                               CONSTRAINT chk_cycle_status CHECK (status IN ('open', 'closed'))
);

CREATE INDEX idx_cycles_dates  ON review_cycles(start_date, end_date);
CREATE INDEX idx_cycles_status ON review_cycles(status);


-- ── PERFORMANCE REVIEWS ──────────────────────────────────────
-- Multiple reviewers (manager, peer, self) can review one employee per cycle.
-- reviewer_id is nullable to support legacy/anonymous reviews.
-- updated_at tracks if a review was edited after submission.
CREATE TABLE performance_reviews (
                                     id              BIGSERIAL PRIMARY KEY,
                                     employee_id     BIGINT          NOT NULL REFERENCES employees(id)     ON DELETE CASCADE,
                                     cycle_id        BIGINT          NOT NULL REFERENCES review_cycles(id) ON DELETE RESTRICT,
                                     reviewer_id     BIGINT          NULL     REFERENCES employees(id)     ON DELETE SET NULL,
                                     review_type     VARCHAR(20)     NOT NULL DEFAULT 'manager',
                                     rating          SMALLINT        NOT NULL,
                                     reviewer_notes  TEXT            NULL,
                                     submitted_at    TIMESTAMP       NOT NULL DEFAULT NOW(),
                                     created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
                                     updated_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
                                     created_by      VARCHAR(255)    NOT NULL DEFAULT 'system',

                                     CONSTRAINT chk_rating_range  CHECK (rating BETWEEN 1 AND 5),
                                     CONSTRAINT chk_review_type   CHECK (review_type IN ('manager', 'peer', 'self'))
);

CREATE INDEX idx_reviews_employee        ON performance_reviews(employee_id);
CREATE INDEX idx_reviews_cycle           ON performance_reviews(cycle_id);
CREATE INDEX idx_reviews_reviewer        ON performance_reviews(reviewer_id);
CREATE INDEX idx_reviews_employee_cycle  ON performance_reviews(employee_id, cycle_id);


-- ── GOALS ────────────────────────────────────────────────────
-- weight (1–100): sum of all goal weights per employee per cycle must = 100
-- before a cycle can be closed. Enforced at application layer on cycle close.
CREATE TABLE goals (
                       id          BIGSERIAL PRIMARY KEY,
                       employee_id BIGINT          NOT NULL REFERENCES employees(id)     ON DELETE CASCADE,
                       cycle_id    BIGINT          NOT NULL REFERENCES review_cycles(id) ON DELETE RESTRICT,
                       title       VARCHAR(255)    NOT NULL,
                       status      VARCHAR(20)     NOT NULL DEFAULT 'pending',
                       weight      INTEGER         NOT NULL DEFAULT 0,
                       created_at  TIMESTAMP       NOT NULL DEFAULT NOW(),
                       updated_at  TIMESTAMP       NOT NULL DEFAULT NOW(),
                       created_by  VARCHAR(255)    NOT NULL DEFAULT 'system',

                       CONSTRAINT chk_goal_status          CHECK (status IN ('pending', 'completed', 'missed')),
                       CONSTRAINT chk_goal_title_not_empty CHECK (TRIM(title) <> ''),
                       CONSTRAINT chk_weight_range         CHECK (weight BETWEEN 1 AND 100)
);

CREATE INDEX idx_goals_employee        ON goals(employee_id);
CREATE INDEX idx_goals_cycle           ON goals(cycle_id);
CREATE INDEX idx_goals_employee_cycle  ON goals(employee_id, cycle_id);
CREATE INDEX idx_goals_status          ON goals(status);


-- ── REVIEW ASSIGNMENTS ───────────────────────────────────────
-- Tracks who was assigned to review whom before reviews are submitted.
-- Only needed if you want to manage peer nominations separately from reviews.
CREATE TABLE review_assignments (
                                    id          BIGSERIAL PRIMARY KEY,
                                    employee_id BIGINT      NOT NULL REFERENCES employees(id)     ON DELETE CASCADE,
                                    reviewer_id BIGINT      NOT NULL REFERENCES employees(id)     ON DELETE CASCADE,
                                    cycle_id    BIGINT      NOT NULL REFERENCES review_cycles(id) ON DELETE RESTRICT,
                                    review_type VARCHAR(20) NOT NULL,
                                    created_at  TIMESTAMP   NOT NULL DEFAULT NOW(),
                                    updated_at  TIMESTAMP   NOT NULL DEFAULT NOW(),
                                    created_by  VARCHAR(255) NOT NULL DEFAULT 'system',

                                    CONSTRAINT uq_assignment         UNIQUE (employee_id, reviewer_id, cycle_id),
                                    CONSTRAINT chk_assignment_type   CHECK (review_type IN ('manager', 'peer', 'self')),
                                    CONSTRAINT chk_no_self_peer      CHECK (
                                        review_type = 'self' OR employee_id <> reviewer_id
                                        )
);

CREATE INDEX idx_assignments_employee ON review_assignments(employee_id);
CREATE INDEX idx_assignments_reviewer ON review_assignments(reviewer_id);
CREATE INDEX idx_assignments_cycle    ON review_assignments(cycle_id);