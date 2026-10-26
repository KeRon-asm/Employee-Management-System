-- =====================================================================
-- 04_app_users.sql -- least-privilege MySQL accounts (replaces root)
--
--   ems_auth      can only read/update logins and write the audit log.
--                 Used before anyone is signed in.
--   ems_employee  read-only (features 1b, 3, 6a, 6d). The service layer
--                 further restricts an employee to their OWN rows.
--   ems_hr        full CRUD on employee data (feature 1a), no schema changes.
--
-- The passwords below are for LOCAL DEVELOPMENT ONLY. Use different
-- ones on any shared or hosted database (e.g. Aiven).
-- =====================================================================

CREATE USER IF NOT EXISTS 'ems_auth'@'%'     IDENTIFIED BY 'auth-local-dev';
CREATE USER IF NOT EXISTS 'ems_employee'@'%' IDENTIFIED BY 'employee-local-dev';
CREATE USER IF NOT EXISTS 'ems_hr'@'%'       IDENTIFIED BY 'hr-local-dev';

-- Authentication account
GRANT SELECT, UPDATE ON employeeData.app_users TO 'ems_auth'@'%';
GRANT INSERT         ON employeeData.audit_log TO 'ems_auth'@'%';

-- General employee account: read-only
GRANT SELECT ON employeeData.employees           TO 'ems_employee'@'%';
GRANT SELECT ON employeeData.payroll             TO 'ems_employee'@'%';
GRANT SELECT ON employeeData.addresses           TO 'ems_employee'@'%';
GRANT SELECT ON employeeData.demographics        TO 'ems_employee'@'%';
GRANT SELECT ON employeeData.cities              TO 'ems_employee'@'%';
GRANT SELECT ON employeeData.states              TO 'ems_employee'@'%';
GRANT SELECT ON employeeData.job_titles          TO 'ems_employee'@'%';
GRANT SELECT ON employeeData.employee_job_titles TO 'ems_employee'@'%';
GRANT SELECT ON employeeData.division            TO 'ems_employee'@'%';
GRANT SELECT ON employeeData.employee_division   TO 'ems_employee'@'%';

-- HR admin account: CRUD on data, but cannot DROP or ALTER tables
GRANT SELECT, INSERT, UPDATE, DELETE ON employeeData.* TO 'ems_hr'@'%';

FLUSH PRIVILEGES;
