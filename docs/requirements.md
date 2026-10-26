# Requirements traceability

How each line of Company Z's user story maps to the design. Use this table when writing programming tasks, test cases, sequence diagrams and the SDD, so everything points at the same classes and tables.

## Client background (from the user story)

| Today | What this system changes |
|---|---|
| One HR admin edits data in DBeaver with raw MySQL scripts | HR uses screens backed by validated, audited services |
| HR admin uses the MySQL **root** account; no other security | App logins with hashed passwords and lockout; three least-privilege MySQL accounts; root is never used |
| Employees only get a PDF pay stub, and a W-2 at year end | Employees sign in to see their own pay history (6a) and personal data (feature 3) |
| ~75 full-time salaried employees, tripling in 24 months | Indexed searches; no per-employee hard-coding; tested with seed data and ready for ~225 |

## Feature map

| # | Requirement | Role | Code | Tables | DB account | Status |
|---|---|---|---|---|---|---|
| 1 | User logon / authorization | All | `AuthService`, `Session`, `PasswordHasher`, `UserDao`, `AuditDao` | `app_users`, `audit_log` | `ems_auth` | Interfaces and hashing done; implementation to do |
| 1a | HR admin: secure CRUD on the entire database | HR_ADMIN | `EmployeeService` (add, update, delete), `EmployeeDao` | all employee tables | `ems_hr` | Interfaces done |
| 1b | Employees see their own pay and demographic data | EMPLOYEE | `EmployeeService.searchForViewing`, `PayHistoryReport` | `employees`, `demographics`, `addresses`, `payroll` | `ems_employee` (read-only) | Interfaces done; 6a done |
| 2 | Search by name, DOB, SSN, empID **for editing** | HR_ADMIN | `EmployeeService.searchForEditing`, `SearchCriteria`, `EmployeeDao.search` | `employees`, `demographics` | `ems_hr` | Interfaces done |
| 3 | Search by name, DOB, SSN, empID **for viewing** | EMPLOYEE | `EmployeeService.searchForViewing` (own record only, SSN masked) | `employees`, `demographics`, `addresses` | `ems_employee` | Interfaces done |
| 4 | Update an employee picked from search results | HR_ADMIN | `EmployeeService.updateEmployee`, `EmployeeDao.update` | all employee tables, one transaction | `ems_hr` | Interfaces done |
| 5 | Raise salary by a % only for salaries in a range | HR_ADMIN | `SalaryRaise.inRange`, `EmployeeService.previewRaise` / `applyRaise`, `EmployeeDao.applyRaise` | `employees`, `audit_log` | `ems_hr` | Rule logic and tests done; DAO to do |
| 6a | Pay statement history, newest pay date first | EMPLOYEE | `PayHistoryReport`, `PayrollDao` | `payroll` | `ems_employee` | Report and tests done; DAO to do |
| 6b | Total pay for a month by job title | HR_ADMIN | `MonthlyPayByJobTitleReport`, `ReportDao.totalPayByJobTitle` | `payroll`, `employee_job_titles`, `job_titles` | `ems_hr` | Report and tests done; DAO to do |
| 6c | Total pay for a month by division | HR_ADMIN | `MonthlyPayByDivisionReport`, `ReportDao.totalPayByDivision` | `payroll`, `employee_division`, `division` | `ems_hr` | Report done; DAO to do |
| 6d | New hires within a date range | **All** | `NewHiresReport`, `DateRange`, `ReportDao.findHiredBetween` | `employees`, job title and division tables | either | Report and tests done; DAO to do |
| 6e | All employees grouped by job title, for management | HR_ADMIN | `EmployeesByJobTitleReport`, `ReportDao.findAllWithJobAndDivision` | `employees`, job title and division tables | `ems_hr` | Report and tests done; DAO to do |
| 7 | MySQL, local or Aiven | — | `DatabaseConfig`, `ConnectionFactory`, `sql/` | — | — | Done (see README for Aiven) |

## Assignment tasks that build on the user story

| Task | Covered by |
|---|---|
| 1.4 3NF tables: `addresses`, `demographics`, `cities`, `states` | `sql/02_normalization.sql` |
| 2a Update employee data, generally, using our classes | `Employee` (+ `Address`, `Demographics`), `EmployeeService.updateEmployee` |
| 2b Search by empID for deletion, confirm, or "no match" | `EmployeeService.findForDeletion` then `deleteEmployee` |
| 2c Raise by % when salary **less than** an amount | `SalaryRaise.below` |
| 3a Raise by % for salaries **in a range** (inputs: percent, min, max) | `SalaryRaise.inRange` |
| 3b Add a new employee, empID auto-generated | `EmployeeService.addEmployee`; `employees.empid` is AUTO_INCREMENT |

## Design decisions and assumptions

Confirm these with the professor if any seem wrong. Each is a one-line change.

1. **Employees view only their own record (feature 3).** Feature 1b says employees see "their" pay and demographic data, and deliverable 1.3 has an employee look up their own empID. So an employee's search returns their own record or "no match", never a co-worker's SSN or DOB.
2. **Report 6a is for general employees,** as the user story says. HR admins can still see anyone's records through search.
3. **Report 6d is open to everyone,** so it shows only hire date, empID, name, job title and division. It never shows salary, SSN, DOB, address or phone.
4. **Salary range (feature 5) includes both ends:** $50,000 to $80,000 includes exactly $80,000. **Task 2c is strict:** "less than $60,000" excludes exactly $60,000.
5. **Raises are 0-100%.** Zero, negative and over-100 values are rejected as likely typos. New salaries round half-up to the cent.
6. **"Total pay for a month"** is the sum of gross `payroll.earnings` for pay dates in that calendar month, shown with employee count and a grand total.
7. **W-2 generation is out of scope.** The user story describes it as the current process but doesn't list it as a feature to build.
8. **All employees are full-time salaried,** so there are no hourly rates or hours in the model.
