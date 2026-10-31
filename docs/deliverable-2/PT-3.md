# PT-3: Payroll and employee reports

| Field | Detail |
|---|---|
| **Requirement** | Feature 6a-6e |
| **Actor / role** | EMPLOYEE (6a), HR_ADMIN (6b, 6c, 6e), or ALL (6d) |
| **Author** | Kenneth Tran |

## Description

This task covers user story feature 3 that generates payroll history and employee reports (Pay Statement History (6a), Total Pay For Month by Job Title (6b), Total Pay For Month by Division (6c), New Employee Hires (6d), and Employees by Job Title (6e)) to allow employees to view their earning and HR to monitor company compensation and employee data.
## Inputs

| Input | Type | Rules |
|---|---|---|
| empID | int | Required for pay history (`findbyEmployeeNewestFirst`) |
| YearMonth | date (YYYY-MM) | Required for monthly total pay (`totalPayByJobTitle`, `totalPayByDivision`) |
| DataRange | date (YYYY-MM-DD) | Required for new employee hires data filtering (`findHiredBetween`) |

## Outputs

Formatted lists/rows displayed via the corresponding report classes depending on user role permissions.

- General Employee
  - `PayHistoryReport` By most recent
- HR Admin
  - `MonthlyPayByJobTitleReport`
  - `MonthlyPayByDivisionReport`
  - `EmployeesByJobTitleReport`
- All Employees
  - `NewHiresReport`
    - Displays only their hire date, employee ID, name, job title, and division.
    - Strictly never shows salary, SSN, date of birth, or phone number.

Nothing in the database changes, because this task only reads data.

## Preconditions
Before this task runs:
- User must be signed in with a valid session and authorized role (EMPLOYEE for 6a/6d, HR for 6b/6c/6e/6d).

## Processing steps

1. Validate user session, permissions, and role authorization for the target report.
2. Open a connection via `ConnectionFactory` using the appropriate database account (`ems_employee` for 6a, `ems_hr` for 6b/6c/6e, either for 6d)
3. Execute SQL queries via `PreparedStatement` to fetch data (`findbyEmployeeNewestFirst`, `totalPayByJobTitle`, `totalPayByDivision`, `findHiredBetween`, `findAllWithJobAndDivision`)
4. Return results to the respective report classes in the `report` package.

## Classes and methods involved

| Layer | Class | Method |
|---|---|---|
| UI | `PayHistoryReport`, `MonthlyPayByJobTitleReport`, `MonthlyPayByDivisionReport`, `EmployeesByJobTitleReport`, `NewHiresReport` | Run methods |
| DAO | `JdbcPayrollDao` | `findByEmployeeNewestFirst(int empID)` |
| DAO | `JdbcReportDao` | `totalPayByJobTitle(YearMonth)`, `totalPayByDivision(YearMonth)`, `findHiredBetween(DataRange)`, `findAllWithJobAndDivision()` |

## Database tables touched

| Table | Read / Insert / Update / Delete |
|---|---|
| `payroll`, `employees`, `employee_job_titles`, `job_titles`, `employee_division`, `division` | Read |

## Error handling

| Situation | What happens |
|---|---|
| Unauthorized role access | Access denied; no data shown |
| No match found | Report displays an empty result set |
| Database error | A DataAccessException is throw; user sees a clear error message |

## Acceptance criteria

- [ ] A general employee able to retrieve statements order by by pay date descending. 
- [ ] A HR admin able to view monthly gross pay for pay dates within the target month.
- [ ] Report 6d displays only hire date, empID, name, job title, and division without exposing salary or SSN.
- [ ] Role-based access controls strictly enforce permissions (6a for employees; 6b, 6c, 6e for HR; 6d for all).
- [ ] No match displays an empty result set; and all three test cases below pass.

## Test cases

### TC-3.1: Employee views own pay history

| Field | Detail |
|---|---|
| **Related PT** | PT-3 |
| **Preconditions / test data** | Signed in as employee `abrooks`; has gross pay of $1,875.00 |
| **Steps** | 1. Log in as `abrooks`, 2. Navigate to Pay History Report, 3. Run the report |
| **Input** | `abrooks` runs Pay History Report |
| **Expected result** | Displays 2 rows, 2026-09-30 first, then 2026-09-15, gross pay $1,875.00 each |
| **Actual result** | *(fill in when run)* |
| **Pass / Fail** | *(fill in when run)* |

### TC-3.2: Employee restricted from running HR pay report

| Field | Detail |
|---|---|
| **Related PT** | PT-3 |
| **Preconditions / test data** | Signed in as employee `abrooks` |
| **Steps** | 1. Log in as `abrooks`, 2. Navigate to Monthly Pay Report by Job Title, 3. Run the report |
| **Input** | `abrooks` runs Monthly Pay Report by Job Title |
| **Expected result** | Displays access denied message, no data shown |
| **Actual result** | *(fill in when run)* |
| **Pass / Fail** | *(fill in when run)* |

### TC-3.3: Employee views new hires report within given data range

| Field | Detail |
|---|---|
| **Related PT** | PT-3 |
| **Preconditions / test data** | Signed in as employee `abrooks` |
| **Steps** | 1. Log in as `abrooks`, 2. Navigate to New Hires Report, 3. Enter data range `2024-01-01` to `2026-12-31` |
| **Input** | `abrooks` runs New Hires Report for `2024-01-01` to `2026-12-31` |
| **Expected result** | Displays 2 rows: 1004 Daniel Okafor (2024-06-17), then 1007 Noah Kim (2026-08-03). No salary or SSN column |
| **Actual result** | *(fill in when run)* |
| **Pass / Fail** | *(fill in when run)* |
