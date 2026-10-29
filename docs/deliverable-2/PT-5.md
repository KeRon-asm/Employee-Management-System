# PT-5: Search by empID and delete an employee (task b)

| Field | Detail |
|---|---|
| **Requirement** | Deliverable task (b): search for an employee by empID for deletion; if found, ask to confirm the delete, otherwise "no match". Also user story feature 1a (HR admin secure CRUD) |
| **Actor / role** | HR_ADMIN |
| **Author** | Member 5 |

## Description

An HR admin removes an employee who has left Company Z. The admin first searches by empID. If the employee exists, the system shows the name and asks for confirmation, and nothing is deleted until the admin clicks Yes. If the empID does not exist, the system says so and stops. Deleting is permanent, which is why the confirmation step is required.

## Inputs

| Input | Type | Rules (required? format? range?) |
|---|---|---|
| empID | Whole number (int) | Required. Must match an existing employee exactly. |
| Confirmation | Yes / No button | Only shown when the empID was found. |
| Session | Session (signed-in user) | Must have the role HR_ADMIN. |

## Outputs

On success (empID found, admin clicks Yes):

- The screen shows "Employee <empID> deleted".
- The employee's rows are removed from `employees`, and automatically from `addresses`, `demographics`, `payroll`, `employee_job_titles`, `employee_division` and `app_users` (their login, if they have one).
- One new row in `audit_log`: action DELETE, table `employees`, record id = the empID, old value = a snapshot of the deleted employee (empID, name, email, hire date, job title, division; the SSN is masked, never stored in full), new value empty.

Other outcomes:

- empID found, admin clicks No: the screen shows "Delete cancelled". Nothing changes in the database.
- empID not found: the screen shows "No match for employee <empID>". No Yes/No prompt. Nothing changes.

## Preconditions

- The user is signed in as HR_ADMIN (PT-1), using the `ems_hr` database account.
- The database is running with the shared test data loaded (sql/05 to sql/07).

## Processing steps

1. The admin opens Delete Employee and types an empID. The screen calls `EmployeeService.findForDeletion(session, empId)`.
2. The service checks the role with `session.requireRole(Role.HR_ADMIN)`. A non-admin gets a SecurityException and the database is never touched.
3. The service calls `EmployeeDao.findById(empId)` and returns the `Optional<Employee>`.
4. Empty result: the screen shows "No match for employee <empID>" and stops (no prompt).
5. Found: the screen shows "Delete employee <empID> <first name> <last name>? Yes / No".
6. If the admin clicks No, the screen shows "Delete cancelled" and stops. `deleteEmployee` is never called.
7. If the admin clicks Yes, the screen calls `EmployeeService.deleteEmployee(session, empId)`.
8. The service checks the role again, then loads the employee with `findById` so the old values are available for the audit row. If it is no longer there (someone else deleted it in the meantime), it returns false.
9. The service calls `EmployeeDao.delete(empId)`, which runs `DELETE FROM employees WHERE empID = ?` with a PreparedStatement. The foreign keys defined with ON DELETE CASCADE in sql/02 and sql/03 remove the address, demographics, payroll, job title, division and login rows automatically.
10. The service writes the DELETE row with `AuditDao.record(userId, DELETE, "employees", empId, oldValue, null)`. The employee delete and the audit insert must succeed or fail together (one transaction). If either fails, both are rolled back.
11. `deleteEmployee` returns true and the screen shows "Employee <empID> deleted".

## Classes and methods involved

| Layer | Class | Method |
|---|---|---|
| UI | Delete Employee screen (still to be built) | Enter empID, show the Yes / No prompt and messages |
| Service | EmployeeServiceImpl (implements EmployeeService) | `Optional<Employee> findForDeletion(Session, int)`, `boolean deleteEmployee(Session, int)` |
| DAO | JdbcEmployeeDao (implements EmployeeDao) | `Optional<Employee> findById(int)` (already written, PT-2), `boolean delete(int)` |
| DAO | JdbcAuditDao (implements AuditDao) | `record(userId, action, tableName, recordId, oldValue, newValue)` |
| Model | Session, Role, Employee | `session.requireRole(Role)` |

## Database tables touched

| Table | Read / Insert / Update / Delete |
|---|---|
| employees | Read (find by empID), Delete |
| addresses | Delete (cascade) |
| demographics | Delete (cascade) |
| payroll | Delete (cascade) |
| employee_job_titles | Delete (cascade) |
| employee_division | Delete (cascade) |
| app_users | Delete (cascade, the employee's login if they have one) |
| audit_log | Insert (one DELETE row). Existing audit rows are never deleted. |
| cities, states, job_titles, division | Not changed. They are shared lookup tables. |

## Error handling

| Situation | What happens |
|---|---|
| Invalid input | empID is not a whole number: the screen asks for a valid employee ID and nothing is called. Caller is not HR_ADMIN: SecurityException, no database call. |
| No match found | "No match for employee <empID>". No Yes/No prompt. Nothing deleted, no audit row. |
| Database error | The delete and the audit row are rolled back, so the employee is not left half deleted. A DataAccessException is thrown and the user sees a clear message. |

## Acceptance criteria

- [ ] Only an HR admin can search for or delete an employee; any other role is refused before the database is used.
- [ ] A found empID shows "Delete employee <empID> <name>? Yes / No", and nothing is deleted until Yes.
- [ ] Yes deletes the employee and every dependent row (address, demographics, payroll, job title, division, login) and writes one DELETE row to audit_log.
- [ ] No leaves the database unchanged and shows "Delete cancelled".
- [ ] An unknown empID shows "No match for employee <empID>" with no prompt and no change.
- [ ] All SQL uses PreparedStatement, and a database error rolls everything back.

---

## Test cases

Shared setup for every test: the shared test data (sql/05 to sql/07), signed in as `hradmin` / `HrAdmin!2026` (HR_ADMIN). empID 9999 does not exist.

**Running order and data reset:** TC-5.1 permanently deletes employee 6, which another test needs (TC-6.1 expects employee 6 at $33,990.00 after the raise). So run the TC-5.x tests last, and reload fresh test data before TC-5.1 and again afterward (`docker compose down -v && docker compose up -d`, see README). Do not just re-run sql/05 to 07 on top of existing data, because it would fail on the duplicate SSNs and emails.

### TC-5.1: Delete an existing employee (confirm Yes)

| Field | Detail |
|---|---|
| **Related PT** | PT-5 (task b) |
| **Preconditions / test data** | Signed in as `hradmin`. Fresh test data: employee 6 (PigPin Dusty) exists with an address, demographics row, payroll rows, job title and division. |
| **Steps** | 1. Open "Delete Employee".<br>2. Enter the empID and click Search.<br>3. Check the prompt shows the employee's name.<br>4. Click Yes. |
| **Input** | empID = 6, then Yes |
| **Expected result** | Prompt reads "Delete employee 6 PigPin Dusty? Yes / No". After Yes the screen shows "Employee 6 deleted". `SELECT COUNT(*) ... WHERE empID = 6` returns 0 for employees, addresses, demographics, payroll, employee_job_titles and employee_division. Employees 1 to 5 are unchanged. `audit_log` has exactly one new row: action DELETE, table_name employees, record_id 6, user_id = hradmin, old_value describing employee 6. |
| **Actual result** | *(fill in when run)* |
| **Pass / Fail** | *(fill in when run)* |

### TC-5.2: Cancel the delete (confirm No)

| Field | Detail |
|---|---|
| **Related PT** | PT-5 (task b) |
| **Preconditions / test data** | Signed in as `hradmin`. Fresh test data: employee 6 exists. |
| **Steps** | 1. Open "Delete Employee".<br>2. Enter the empID and click Search.<br>3. Click No at the prompt. |
| **Input** | empID = 6, then No |
| **Expected result** | The screen shows "Delete cancelled". Employee 6 still exists in employees, addresses, demographics, payroll, employee_job_titles and employee_division, with the same values as before. No new row in `audit_log`. |
| **Actual result** | *(fill in when run)* |
| **Pass / Fail** | *(fill in when run)* |

### TC-5.3: empID not found

| Field | Detail |
|---|---|
| **Related PT** | PT-5 (task b) |
| **Preconditions / test data** | Signed in as `hradmin`. Fresh test data. empID 9999 does not exist. |
| **Steps** | 1. Open "Delete Employee".<br>2. Enter the empID and click Search. |
| **Input** | empID = 9999 |
| **Expected result** | The screen shows "No match for employee 9999". No Yes/No prompt appears. Row counts in employees and every dependent table are the same as before. No new row in `audit_log`. |
| **Actual result** | *(fill in when run)* |
| **Pass / Fail** | *(fill in when run)* |

### TC-5.4: An employee cannot delete (extra)

| Field | Detail |
|---|---|
| **Related PT** | PT-5 (task b), role check |
| **Preconditions / test data** | Signed in as `sbeagle` / `Employee!2026` (EMPLOYEE, empID 1). Fresh test data. |
| **Steps** | 1. Try to run the delete search for employee 6 (the Delete Employee screen is not offered to employees; this is checked by calling `findForDeletion` and `deleteEmployee` directly with the employee's Session). |
| **Input** | empID = 6 |
| **Expected result** | A SecurityException is thrown for both methods, no employee data is returned, and employee 6 still exists. No new row in `audit_log`. |
| **Actual result** | *(fill in when run)* |
| **Pass / Fail** | *(fill in when run)* |
