-- =====================================================================
-- 01_base_schema.sql -- the original employeeData tables
--
-- !! RECONCILE THIS FILE WITH THE PROFESSOR'S employeeData SCRIPT !!
-- This is a reconstruction so the project builds and runs today.
-- If the provided script differs (column names, types, keys), replace
-- this file's contents with it and adjust 02-05 to match.
--
-- One deliberate change: employees.empid is AUTO_INCREMENT, because
-- deliverable 3b requires empID to be auto-generated.
-- =====================================================================

CREATE DATABASE IF NOT EXISTS employeeData;
USE employeeData;

CREATE TABLE employees (
    empid      INT           NOT NULL AUTO_INCREMENT,
    Fname      VARCHAR(65)   NOT NULL,
    Lname      VARCHAR(65)   NOT NULL,
    email      VARCHAR(65)   NOT NULL,
    HireDate   DATE          NOT NULL,
    Salary     DECIMAL(10,2) NOT NULL,
    SSN        VARCHAR(11)   NOT NULL,
    PRIMARY KEY (empid),
    UNIQUE KEY uq_employees_ssn (SSN),
    UNIQUE KEY uq_employees_email (email)
) AUTO_INCREMENT = 1001;

CREATE TABLE job_titles (
    job_title_id INT          NOT NULL,
    job_title    VARCHAR(125) NOT NULL,
    PRIMARY KEY (job_title_id)
);

CREATE TABLE employee_job_titles (
    empid        INT NOT NULL,
    job_title_id INT NOT NULL,
    PRIMARY KEY (empid),
    FOREIGN KEY (empid)        REFERENCES employees (empid) ON DELETE CASCADE,
    FOREIGN KEY (job_title_id) REFERENCES job_titles (job_title_id)
);

CREATE TABLE division (
    ID           INT          NOT NULL,
    Name         VARCHAR(100) NOT NULL,
    city         VARCHAR(50)  NOT NULL,
    addressLine1 VARCHAR(50)  NOT NULL,
    addressLine2 VARCHAR(50)  NULL,
    state        VARCHAR(50)  NULL,
    country      VARCHAR(50)  NOT NULL,
    postalCode   VARCHAR(15)  NOT NULL,
    PRIMARY KEY (ID)
);

CREATE TABLE employee_division (
    empid  INT NOT NULL,
    div_ID INT NOT NULL,
    PRIMARY KEY (empid),
    FOREIGN KEY (empid)  REFERENCES employees (empid) ON DELETE CASCADE,
    FOREIGN KEY (div_ID) REFERENCES division (ID)
);

CREATE TABLE payroll (
    payID       INT           NOT NULL AUTO_INCREMENT,
    pay_date    DATE          NOT NULL,
    earnings    DECIMAL(10,2) NOT NULL,
    fed_tax     DECIMAL(10,2) NOT NULL,
    fed_med     DECIMAL(10,2) NOT NULL,
    fed_SS      DECIMAL(10,2) NOT NULL,
    state_tax   DECIMAL(10,2) NOT NULL,
    retire_401k DECIMAL(10,2) NOT NULL,
    health_care DECIMAL(10,2) NOT NULL,
    empid       INT           NOT NULL,
    PRIMARY KEY (payID),
    FOREIGN KEY (empid) REFERENCES employees (empid) ON DELETE CASCADE
);
