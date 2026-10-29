# PT-2: HR admin searches for employees to edit

| Field | Detail |
|---|---|
| **Requirement** | User story feature 2: search for employee data by name, DOB, SSN or empID to show their information for EDITING (HR admin only) |
| **Actor / role** | HR_ADMIN |
| **Author** | Member 2 |

## Description

The HR admin types one or more search values, and the system shows every matching employee in a list. Picking a row opens that employee for editing (PT-4). Only an HR admin can use this search, because the results include SSNs and dates of birth.

## Inputs

| Input | Type | Rules (required? format? range?) |
|---|---|---|
| Employee ID | Whole number (int) | Optional. Must match exactly. |
| Name | Text | Optional. Matches part of a first name, last name or full name. Not case-sensitive ("brown" finds Charlie Brown). |
| Date of birth | Date | Optional. Typed as YYYY-MM-DD. Must match exactly. |
| SSN | Text | Optional. Must match exactly. Can be typed with or without dashes (111-33-1111 or 111331111). |
| Session | Session (signed-in user) | Must have the role HR_ADMIN. |

At least one search field is required. If more than one is filled in, an employee must match all of them.

## Outputs

On success (at least one match):

- A list of matching employees, sorted by last name, then first name.
- Each row shows employee ID, name, job title, division and hire date.
- Picking a row opens that employee for editing (PT-4).

Other outcomes:

- No match: the screen shows "No matching employee" and an empty list.

Nothing in the database changes, because this task only reads data.

## Preconditions

- The user is signed in as HR_ADMIN (PT-1), using the `ems_hr` database account.
- The database is running with the shared test data loaded (sql/05 to sql/07).

## Processing steps

1. The HR admin opens the employee search screen, enters one or more values and clicks Search.
2. The screen puts the values in a `SearchCriteria` object and calls `EmployeeService.searchForEditing(session, criteria)`.
3. The service checks the role with `session.requireRole(Role.HR_ADMIN)`. A non-admin gets a SecurityException and the database is never touched.
4. The service checks that at least one field was filled in. If not, it rejects the search with "Enter at least one search value: name, date of birth, SSN or employee ID".
5. The service calls `EmployeeDao.search(criteria)`.
6. `JdbcEmployeeDao` builds one SELECT joining employees, demographics, addresses, cities, states, job titles and divisions, with one condition for each field that was given.
7. Every typed value is bound as a ? placeholder in a PreparedStatement, so typed text can never run as SQL. The results are ordered by last name, first name, then employee ID.
8. Each row is turned into an `Employee` object (with its `Address` and `Demographics`) and the list is returned.
9. The screen shows the list, or "No matching employee" if the list is empty.

## Classes and methods involved

| Layer | Class | Method |
|---|---|---|
| UI | Employee search screen (still to be built) | Enter search values, show the results list |
| Service | EmployeeServiceImpl (implements EmployeeService) | `List<Employee> searchForEditing(Session, SearchCriteria)` |
| DAO | JdbcEmployeeDao (implements EmployeeDao) | `List<Employee> search(SearchCriteria)`, `Optional<Employee> findById(int)`, helpers `buildSearchSql`, `normalizeSsn`, `escapeLike`, `mapEmployee` |
| Model | SearchCriteria, Employee, Address, Demographics, Session, Role | |

## Database tables touched

| Table | Read / Insert / Update / Delete |
|---|---|
| employees | Read |
| demographics, addresses | Read |
| cities, states | Read (lookup tables) |
| employee_job_titles, job_titles | Read |
| employee_division, division | Read |

## Error handling

| Situation | What happens |
|---|---|
| Invalid input | Nothing entered: the user sees "Enter at least one search value: name, date of birth, SSN or employee ID" and the database is not called. Caller is not HR_ADMIN: SecurityException, no database call. Text containing %, _ or SQL such as '; DROP TABLE is treated as plain text to look for and never runs as SQL. |
| No match found | "No matching employee" and an empty list. Nothing changes. |
| Database error | A DataAccessException ("Could not search employees") is thrown and the screen shows a clear message. Nothing changes, because the search only reads. |

## Acceptance criteria

- [ ] An HR admin can search by name, date of birth, SSN or employee ID, alone or combined.
- [ ] A name search matches part of a name in any letter case, and an SSN search works with or without dashes.
- [ ] A non-HR user cannot run the search, and a search with nothing entered is refused.
- [ ] No match shows "No matching employee", and all three test cases below pass.

---

## Test cases

Shared setup for every test: the shared test data (sql/05 to sql/07), freshly loaded, signed in as `hradmin` / `HrAdmin!2026` (HR_ADMIN). empID 9999 does not exist.

### TC-2.1: Search by name finds one employee

| Field | Detail |
|---|---|
| **Related PT** | PT-2 (feature 2) |
| **Preconditions / test data** | Signed in as `hradmin`. Fresh test data: employee 2 is Charlie Brown, the only employee whose name contains "brown". |
| **Steps** | 1. Open the employee search screen.<br>2. Type the name and leave the other fields empty.<br>3. Click Search. |
| **Input** | Name = "brown" |
| **Expected result** | Exactly one result: employee 2, Charlie Brown, Chief Exec. Officer, Systems, hired 2022-07-01. |
| **Actual result** | *(fill in when run)* |
| **Pass / Fail** | *(fill in when run)* |

### TC-2.2: Search by SSN finds one employee

| Field | Detail |
|---|---|
| **Related PT** | PT-2 (feature 2) |
| **Preconditions / test data** | Signed in as `hradmin`. Fresh test data: SSN 111-33-1111 belongs to employee 3, Lucy Doctor. |
| **Steps** | 1. Open the employee search screen.<br>2. Type the SSN and leave the other fields empty.<br>3. Click Search.<br>4. Search again with the SSN typed without dashes. |
| **Input** | SSN = "111-33-1111", then SSN = "111331111" |
| **Expected result** | Both searches return exactly one result: employee 3, Lucy Doctor, Chief Finn. Officer, C-Suite, hired 2022-07-03. |
| **Actual result** | *(fill in when run)* |
| **Pass / Fail** | *(fill in when run)* |

### TC-2.3: Search for an employee that does not exist

| Field | Detail |
|---|---|
| **Related PT** | PT-2 (feature 2) |
| **Preconditions / test data** | Signed in as `hradmin`. Fresh test data: employee ID 9999 does not exist. |
| **Steps** | 1. Open the employee search screen.<br>2. Type the employee ID and leave the other fields empty.<br>3. Click Search. |
| **Input** | Employee ID = 9999 |
| **Expected result** | The message "No matching employee" and an empty results list. Nothing in the database changes. |
| **Actual result** | *(fill in when run)* |
| **Pass / Fail** | *(fill in when run)* |
