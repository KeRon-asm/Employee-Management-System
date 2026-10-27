-- =====================================================================
-- 02_normalization.sql -- everything we add on top of the professor's tables
--
-- Runs right after 01_employeeData_create.sql and BEFORE any data is loaded.
--
-- Part A: the professor's tables have no keys at all. We add primary keys,
--         foreign keys (with ON DELETE CASCADE so deleting an employee removes
--         their rows everywhere) and AUTO_INCREMENT, because deliverable 3b
--         requires an auto-generated empID.
-- Part B: deliverable 1.4 -- states, cities, addresses, demographics (3NF).
-- Part C: indexes on every search field (name, DOB, SSN, empID).
--
-- Column names follow the professor's script exactly (job_titleID, divID, ...).
-- =====================================================================
USE employeeData;

-- ---------------------------------------------------------------------
-- Part A: keys on the professor's tables
-- ---------------------------------------------------------------------
ALTER TABLE employees
    ADD PRIMARY KEY (empID),
    MODIFY empID INT NOT NULL AUTO_INCREMENT,
    ADD UNIQUE KEY uq_employees_ssn (SSN),
    ADD UNIQUE KEY uq_employees_email (email);

ALTER TABLE job_titles ADD PRIMARY KEY (job_titleID);
ALTER TABLE division   ADD PRIMARY KEY (divID);

-- One job title and one division per employee (the design assumes this).
ALTER TABLE employee_job_titles ADD PRIMARY KEY (empID);
ALTER TABLE employee_job_titles
    ADD CONSTRAINT fk_ejt_employee FOREIGN KEY (empID)       REFERENCES employees (empID)        ON DELETE CASCADE,
    ADD CONSTRAINT fk_ejt_title    FOREIGN KEY (job_titleID) REFERENCES job_titles (job_titleID);

ALTER TABLE employee_division ADD PRIMARY KEY (empID);
ALTER TABLE employee_division
    ADD CONSTRAINT fk_ed_employee FOREIGN KEY (empID) REFERENCES employees (empID) ON DELETE CASCADE,
    ADD CONSTRAINT fk_ed_division FOREIGN KEY (divID) REFERENCES division (divID);

-- payID was a plain INT, so the professor's payroll INSERTs leave it NULL.
-- Making it AUTO_INCREMENT numbers every pay statement automatically.
ALTER TABLE payroll
    MODIFY payID INT NOT NULL AUTO_INCREMENT,
    MODIFY empID INT NOT NULL,
    ADD PRIMARY KEY (payID);
ALTER TABLE payroll
    ADD CONSTRAINT fk_payroll_employee FOREIGN KEY (empID) REFERENCES employees (empID) ON DELETE CASCADE;

-- ---------------------------------------------------------------------
-- Part B: deliverable 1.4 (third normal form)
-- ---------------------------------------------------------------------
CREATE TABLE states (
    stateID      INT     NOT NULL AUTO_INCREMENT,
    abbreviation CHAR(2) NOT NULL,
    PRIMARY KEY (stateID),
    UNIQUE KEY uq_states_abbreviation (abbreviation)
);

CREATE TABLE cities (
    cityID    INT         NOT NULL AUTO_INCREMENT,
    city_name VARCHAR(80) NOT NULL,
    PRIMARY KEY (cityID)
);

CREATE TABLE addresses (
    empID   INT          NOT NULL,
    street  VARCHAR(120) NOT NULL,
    cityID  INT          NOT NULL,
    stateID INT          NOT NULL,
    zip     VARCHAR(10)  NOT NULL,
    PRIMARY KEY (empID),
    FOREIGN KEY (empID)   REFERENCES employees (empID) ON DELETE CASCADE,
    FOREIGN KEY (cityID)  REFERENCES cities (cityID),
    FOREIGN KEY (stateID) REFERENCES states (stateID)
);

CREATE TABLE demographics (
    empID                   INT          NOT NULL,
    DOB                     DATE         NOT NULL,
    mobile_phone            VARCHAR(20)  NOT NULL,
    emergency_contact_name  VARCHAR(130) NOT NULL,
    emergency_contact_phone VARCHAR(20)  NOT NULL,
    PRIMARY KEY (empID),
    FOREIGN KEY (empID) REFERENCES employees (empID) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- Part C: search and report indexes (SSN and empID are already indexed by their keys)
-- ---------------------------------------------------------------------
CREATE INDEX idx_employees_name    ON employees (Lname, Fname);
CREATE INDEX idx_employees_hire    ON employees (HireDate);
CREATE INDEX idx_employees_salary  ON employees (Salary);
CREATE INDEX idx_demographics_dob  ON demographics (DOB);
CREATE INDEX idx_payroll_emp_date  ON payroll (empID, pay_date);
