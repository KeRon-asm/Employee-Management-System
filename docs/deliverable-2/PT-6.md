# PT-6: Raise Salary When Salary Is Less Than an Amount

| Field | Detail |
|---|---|
| Requirement | Deliverable 2, task (c): update salary by a given percentage for every employee whose current salary is less than a specified amount. Related to user story feature 5. |
| Actor / role | `HR_ADMIN` only |
| Author | Member 6 |

## Description

Allow an authorized HR administrator to enter a positive raise percentage and a salary threshold. The system raises salaries **strictly below** that threshold in the MySQL `employees` table and reports the number of employees updated; salaries equal to or above the threshold remain unchanged.

## Inputs

| Input | Type | Rules (required? format? range?) |
|---|---|---|
| Signed-in session | `Session` | Required; role must be `HR_ADMIN`. |
| Raise percentage | `BigDecimal` | Required; must be greater than zero; e.g., `3.00` for 3%. |
| Salary threshold | `BigDecimal` | Required; positive monetary amount; e.g., `48000.00`. |
| Raise request | `SalaryRaise` | Constructed from validated inputs using `SalaryRaise.below(percent, threshold)`. |

## Outputs

On success, show **`<count> employees updated`**, using the number of affected employee rows reported by the database. Every qualifying employee receives a salary increase rounded to two decimal places; all other salaries stay unchanged. For the shared seed data, a 3% raise below $48,000 produces **`3 employees updated`**.

## Preconditions

- The user is signed in as `HR_ADMIN` (sample login: `hradmin`).
- The `employeeData` MySQL database and its `employees` table are available.
- Test cases start with a fresh copy of the shared data (`sql/05`, `sql/06`, and `sql/07`), or salaries are reset before each case. Tests must not reuse salaries modified by a previous case.

## Processing steps

1. The HR administrator opens the salary increase task and enters a raise percentage and threshold.
2. Validate the inputs before changing the database. Reject percentages less than or equal to zero with **`Raise percentage must be greater than 0`**.
3. Build the request with `SalaryRaise.below(percent, threshold)` and pass the signed-in `Session` and request to `EmployeeServiceImpl.applyRaise(Session, SalaryRaise)`.
4. In the service layer, verify the session has role `HR_ADMIN`; deny any unauthorized request without modifying data.
5. Call `JdbcEmployeeDao.applyRaise(SalaryRaise)`. Use the authorized HR database connection, a `PreparedStatement` with parameterized percentage and threshold, and a transaction to execute the update:

   ```sql
   UPDATE employees
   SET Salary = ROUND(Salary * (1 + ? / 100), 2)
   WHERE Salary < ?;
   ```

6. Use the strict `<` comparison: a salary **exactly equal to** the threshold is excluded. Obtain the affected-row count from the update.
7. Commit the transaction if successful; roll it back if the database operation fails. Consistent with the repository's audit requirement, record each salary change through the audit DAO with its old and new values as part of the successful operation.
8. Display **`<count> employees updated`**. Do not expose SQL errors or unnecessary employee information to an unauthorized user.

## Classes and methods involved

| Layer | Class | Method |
|---|---|---|
| UI | Salary increase screen / console handler (to be implemented) | Collect percentage and threshold; display result |
| Model | `SalaryRaise` | `below(percent, threshold)` |
| Service | `service/EmployeeServiceImpl.java` | `applyRaise(Session, SalaryRaise)` |
| DAO | `dao/jdbc/JdbcEmployeeDao.java` | `applyRaise(SalaryRaise)` |
| DAO (auditing) | `JdbcAuditDao` through `AuditDao` | `record(...)` for successful updates |

## Database tables touched

| Table | Read / Insert / Update / Delete |
|---|---|
| `employees` | Read original salaries as needed for audit; **Update** `Salary` for qualifying rows |
| `audit_log` | Insert records of successful salary changes per repository audit requirements |

## Error handling

| Situation | What happens |
|---|---|
| Raise percentage is zero or negative | Show **`Raise percentage must be greater than 0`**; change nothing. |
| Missing or invalid threshold / percentage | Reject input before database update; change nothing. |
| Signed-in user is not `HR_ADMIN` | Access denied; no salary information changed. |
| No match found (`Salary < threshold` matches no employee) | Show **`0 employees updated`**; all salaries unchanged. |
| Salary is exactly the threshold | Excluded from the update; remains unchanged. |
| Database error | Roll back the transaction; show a clear error message and do not report success. |

## Acceptance criteria

- [ ] Only an authenticated `HR_ADMIN` can execute the operation.
- [ ] A positive percentage and valid threshold are required.
- [ ] Only salaries **strictly less than** the threshold are updated.
- [ ] New salaries are calculated as `ROUND(oldSalary * (1 + percent/100), 2)`.
- [ ] The update is transactional and uses a parameterized `PreparedStatement`.
- [ ] The user sees the correct count of updated employees.
- [ ] Invalid inputs cause no salary changes.
- [ ] Changes are recorded in `audit_log` as required by the project's auditing design.

---

# Test Cases for PT-6

The following are **written test specifications**, not results from a running application. Complete `Actual result` and `Pass / Fail` only after execution. **Reset the database to the shared seed data before each test.**

### TC-6.1: Apply a 3% raise to salaries below $48,000

| Field | Detail |
|---|---|
| Related PT | PT-6 (task c) |
| Preconditions / test data | Signed in as `hradmin` (`HR_ADMIN`). Fresh shared data: employee 1 salary = $45,000.00; employee 5 = $43,000.00; employee 6 = $33,000.00. |
| Steps | 1. Open the salary increase task.<br>2. Enter percentage `3` and threshold `48000`.<br>3. Apply the raise.<br>4. Inspect the confirmation message and database salaries. |
| Input | Percentage = `3.00`; threshold = `48000.00` (`SalaryRaise.below(3.00, 48000.00)`). |
| Expected result | Message **`3 employees updated`**. Employee 1: **$46,350.00**; employee 5: **$44,290.00**; employee 6: **$33,990.00**. No other employee's salary changes; successful changes are auditable. |
| Actual result | _To be completed when test is run._ |
| Pass / Fail | _To be completed when test is run._ |

### TC-6.2: Employees at or above the threshold are excluded

| Field | Detail |
|---|---|
| Related PT | PT-6 (task c) |
| Preconditions / test data | Signed in as `hradmin` (`HR_ADMIN`). **Fresh shared data** (not the already-raised state from TC-6.1). Employee 2 salary = $48,000.00; employee 3 = $55,000.00; employee 4 = $98,000.00. |
| Steps | 1. Open the salary increase task.<br>2. Enter percentage `3` and threshold `48000`.<br>3. Apply the raise.<br>4. Verify salaries for employees 2, 3, and 4. |
| Input | Percentage = `3.00`; threshold = `48000.00`. |
| Expected result | Message **`3 employees updated`** (employees 1, 5, and 6 only). Employee 2 remains **$48,000.00** (exact boundary); employee 3 remains **$55,000.00**; employee 4 remains **$98,000.00**. |
| Actual result | _To be completed when test is run._ |
| Pass / Fail | _To be completed when test is run._ |

### TC-6.3: Reject a negative raise percentage

| Field | Detail |
|---|---|
| Related PT | PT-6 (task c) |
| Preconditions / test data | Signed in as `hradmin` (`HR_ADMIN`). Fresh shared data; record current salary values before testing. |
| Steps | 1. Open the salary increase task.<br>2. Enter percentage `-5` and threshold `48000`.<br>3. Attempt to apply the raise.<br>4. Check the error and verify the stored salaries. |
| Input | Percentage = `-5.00`; threshold = `48000.00`. |
| Expected result | Show **`Raise percentage must be greater than 0`**. No salaries are changed and no successful salary-update audit records are created. |
| Actual result | _To be completed when test is run._ |
| Pass / Fail | _To be completed when test is run._ |
