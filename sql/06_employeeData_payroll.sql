INSERT INTO payroll (pay_date, empID, earnings, fed_tax, fed_med, fed_SS, state_tax, retire_401k, health_care)
SELECT 
	'2025-01-31', 
	1, 
	salary/52.0, 
	(salary/52.0)*0.32, 
	(salary/52.0)*0.0145,
	(salary/52.0)*0.062,
	(salary/52.0)*0.12,
	(salary/52.0)*0.004,
	(salary/52.0)*0.031
FROM employees WHERE employees.empID=1;

INSERT INTO payroll (pay_date, empID, earnings, fed_tax, fed_med, fed_SS, state_tax, retire_401k, health_care)
SELECT 
	'2025-02-28', 
	1, 
	salary/52.0, 
	(salary/52.0)*0.32, 
	(salary/52.0)*0.0145, 
	(salary/52.0)*0.062,
	(salary/52.0)*0.12,
	(salary/52.0)*0.004,
	(salary/52.0)*0.031 
FROM employees WHERE employees.empID=1;

INSERT INTO payroll (pay_date, empID, earnings, fed_tax, fed_med, fed_SS, state_tax, retire_401k, health_care)
SELECT 
	'2024-12-31', 
	1, 
	salary/52.0, 
	(salary/52.0)*0.32, 
	(salary/52.0)*0.0145, 
	(salary/52.0)*0.062,
	(salary/52.0)*0.12,
	(salary/52.0)*0.004,
	(salary/52.0)*0.031 
FROM employees WHERE employees.empID=1;

INSERT INTO payroll (pay_date, empID, earnings, fed_tax, fed_med, fed_SS, state_tax, retire_401k, health_care)
SELECT 
	'2024-11-30', 
	1, 
	salary/52.0, 
	(salary/52.0)*0.32, 
	(salary/52.0)*0.0145, 
	(salary/52.0)*0.062,
	(salary/52.0)*0.12,
	(salary/52.0)*0.004,
	(salary/52.0)*0.031 
FROM employees WHERE employees.empID=1;

INSERT INTO payroll (pay_date, empID, earnings, fed_tax, fed_med, fed_SS, state_tax, retire_401k, health_care)
SELECT 
	'2025-01-31', 
	2, 
	salary/52.0, 
	(salary/52.0)*0.32, 
	(salary/52.0)*0.0145,
	(salary/52.0)*0.062,
	(salary/52.0)*0.12,
	(salary/52.0)*0.004,
	(salary/52.0)*0.031
FROM employees WHERE  employees.empID=2;

INSERT INTO payroll (pay_date, empID, earnings, fed_tax, fed_med, fed_SS, state_tax, retire_401k, health_care)
SELECT 
	'2025-02-28', 
	2, 
	salary/52.0, 
	(salary/52.0)*0.32, 
	(salary/52.0)*0.0145, 
	(salary/52.0)*0.062,
	(salary/52.0)*0.12,
	(salary/52.0)*0.004,
	(salary/52.0)*0.031 
FROM employees WHERE  employees.empID=2;

INSERT INTO payroll (pay_date, empID, earnings, fed_tax, fed_med, fed_SS, state_tax, retire_401k, health_care)
SELECT 
	'2025-01-31', 
	3, 
	salary/52.0, 
	(salary/52.0)*0.32, 
	(salary/52.0)*0.0145,
	(salary/52.0)*0.062,
	(salary/52.0)*0.12,
	(salary/52.0)*0.004,
	(salary/52.0)*0.031
FROM employees WHERE employees.empID=3;

INSERT INTO payroll (pay_date, empID, earnings, fed_tax, fed_med, fed_SS, state_tax, retire_401k, health_care)
SELECT 
	'2025-02-28', 
	3, 
	salary/52.0, 
	(salary/52.0)*0.32, 
	(salary/52.0)*0.0145, 
	(salary/52.0)*0.062,
	(salary/52.0)*0.12,
	(salary/52.0)*0.004,
	(salary/52.0)*0.031 
FROM employees WHERE employees.empID=3;

INSERT INTO payroll (pay_date, empID, earnings, fed_tax, fed_med, fed_SS, state_tax, retire_401k, health_care)
SELECT 
	'2025-01-31', 
	4, 
	salary/52.0, 
	(salary/52.0)*0.32, 
	(salary/52.0)*0.0145, 
	(salary/52.0)*0.062,
	(salary/52.0)*0.12,
	(salary/52.0)*0.004,
	(salary/52.0)*0.031 
FROM employees WHERE employees.empID=4;

INSERT INTO payroll (pay_date, empID, earnings, fed_tax, fed_med, fed_SS, state_tax, retire_401k, health_care)
SELECT 
	'2025-02-28', 
	4, 
	salary/52.0, 
	(salary/52.0)*0.32, 
	(salary/52.0)*0.0145, 
	(salary/52.0)*0.062,
	(salary/52.0)*0.12,
	(salary/52.0)*0.004,
	(salary/52.0)*0.031 
FROM employees WHERE employees.empID=4;

INSERT INTO payroll (pay_date, empID, earnings, fed_tax, fed_med, fed_SS, state_tax, retire_401k, health_care)
SELECT 
	'2025-01-31', 
	5, 
	salary/52.0, 
	(salary/52.0)*0.32, 
	(salary/52.0)*0.0145,
	(salary/52.0)*0.062,
	(salary/52.0)*0.12,
	(salary/52.0)*0.004,
	(salary/52.0)*0.031
FROM employees WHERE employees.empID=5;

INSERT INTO payroll (pay_date, empID, earnings, fed_tax, fed_med, fed_SS, state_tax, retire_401k, health_care)
SELECT 
	'2025-02-28', 
	5, 
	salary/52.0, 
	(salary/52.0)*0.32, 
	(salary/52.0)*0.0145, 
	(salary/52.0)*0.062,
	(salary/52.0)*0.12,
	(salary/52.0)*0.004,
	(salary/52.0)*0.031 
FROM employees WHERE employees.empID=5;

INSERT INTO payroll (pay_date, empID, earnings, fed_tax, fed_med, fed_SS, state_tax, retire_401k, health_care)
SELECT 
	'2025-01-31', 
	6, 
	salary/52.0, 
	(salary/52.0)*0.32, 
	(salary/52.0)*0.0145,
	(salary/52.0)*0.062,
	(salary/52.0)*0.12,
	(salary/52.0)*0.004,
	(salary/52.0)*0.031
FROM employees WHERE employees.empID=6;

INSERT INTO payroll (pay_date, empID, earnings, fed_tax, fed_med, fed_SS, state_tax, retire_401k, health_care)
SELECT 
	'2025-02-28', 
	6, 
	salary/52.0, 
	(salary/52.0)*0.32, 
	(salary/52.0)*0.0145, 
	(salary/52.0)*0.062,
	(salary/52.0)*0.12,
	(salary/52.0)*0.004,
	(salary/52.0)*0.031 
FROM employees WHERE employees.empID=6;


