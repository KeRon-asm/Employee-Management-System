-- =====================================================================
-- 02_normalization.sql -- deliverable 1.4: tables for third normal form
--   states, cities, addresses, demographics
-- plus indexes on every search field (name, DOB, SSN, empid) so lookups
-- stay fast as headcount grows from ~75 to ~225.
-- =====================================================================
USE employeeData;

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
    empid   INT          NOT NULL,
    street  VARCHAR(120) NOT NULL,
    cityID  INT          NOT NULL,
    stateID INT          NOT NULL,
    zip     VARCHAR(10)  NOT NULL,
    PRIMARY KEY (empid),
    FOREIGN KEY (empid)   REFERENCES employees (empid) ON DELETE CASCADE,
    FOREIGN KEY (cityID)  REFERENCES cities (cityID),
    FOREIGN KEY (stateID) REFERENCES states (stateID)
);

CREATE TABLE demographics (
    empid                   INT         NOT NULL,
    DOB                     DATE        NOT NULL,
    mobile_phone            VARCHAR(20) NOT NULL,
    emergency_contact_name  VARCHAR(130) NOT NULL,
    emergency_contact_phone VARCHAR(20) NOT NULL,
    PRIMARY KEY (empid),
    FOREIGN KEY (empid) REFERENCES employees (empid) ON DELETE CASCADE
);

-- Search indexes (SSN and empid are already indexed by their keys)
CREATE INDEX idx_employees_name    ON employees (Lname, Fname);
CREATE INDEX idx_employees_hire    ON employees (HireDate);
CREATE INDEX idx_employees_salary  ON employees (Salary);
CREATE INDEX idx_demographics_dob  ON demographics (DOB);
CREATE INDEX idx_payroll_emp_date  ON payroll (empid, pay_date);
