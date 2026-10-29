# PT-8: Employee views their own record

| Field | Detail |
|---|---|
| **Requirement** | User story feature 3: search for employee data by name, DOB, SSN or empID to show their information for VIEWING (general employee only). Also feature 1b (employees see their own pay and demographic data). Extra task: not required for 11/1, test cases included anyway |
| **Actor / role** | EMPLOYEE |
| **Author** | Member 2 |

## Description

A signed-in employee looks up their own information, but only their own: nobody can see a co-worker's SSN, date of birth or pay. The screen is read-only, and the SSN is always masked.

## Inputs

| Input | Type | Rules (required? format? range?) |
|---|---|---|
| Employee ID | Whole number (int) | Optional. Must be the employee's own ID. |
| Name | Text | Optional. Must match the employee's own name (same matching as PT-2). |
| Date of birth | Date | Optional. YYYY-MM-DD. Must be the employee's own. |
| SSN | Text | Optional. With or without dashes. Must be the employee's own. |
| Session | Session (signed-in user) | Must have the role EMPLOYEE and be linked to an employee record. |

All fields are optional. Leaving every field empty simply shows the employee's own record.

## Outputs

On success (the employee's own record matches):

- A read-only view of the record:
  - Personal: name, email, date of birth, mobile phone, emergency contact, address.
  - Job: job title, division, hire date, salary.
  - SSN masked as ***-**-NNNN (last four digits only).

Other outcomes:

- Anything else (a co-worker's ID, name or SSN): the message "No matching employee". It is the same message every time, so the search cannot be used to find out who works at the company.

Nothing in the database changes, because this task only reads data.

## Preconditions

- The user is signed in as EMPLOYEE (PT-1), and the account is linked to an employee record.
- The database is running with the shared test data loaded (sql/05 to sql/07).

## Processing steps

1. The employee opens the search screen, enters values (or none) and clicks Search.
2. The screen calls `EmployeeService.searchForViewing(session, criteria)`.
3. The service checks the role with `session.requireRole(Role.EMPLOYEE)`. Anyone else gets a SecurityException.
4. If the employee typed an employee ID that is not their own, the service returns "no match" straight away, without calling the database.
5. Otherwise the service replaces the employee ID with the signed-in employee's own ID, keeps whatever else was typed, and calls `EmployeeDao.search(criteria)`.
6. `JdbcEmployeeDao` runs the same query as PT-2, using the read-only `ems_employee` account.
7. If a row comes back, the service returns a copy with the SSN masked by `SsnMasker`. The stored record is unchanged.
8. The screen shows the record, or "No matching employee".

## Classes and methods involved

| Layer | Class | Method |
|---|---|---|
| UI | Search screen in view mode (still to be built) | Enter search values, show the read-only record |
| Service | EmployeeServiceImpl (implements EmployeeService) | `Optional<Employee> searchForViewing(Session, SearchCriteria)` |
| DAO | JdbcEmployeeDao (implements EmployeeDao) | `List<Employee> search(SearchCriteria)`, the same method PT-2 uses |
| Security | SsnMasker | `mask(String)` |
| Model | SearchCriteria, Employee, Session | |

## Database tables touched

| Table | Read / Insert / Update / Delete |
|---|---|
| employees | Read (own row only) |
| demographics, addresses | Read (own row only) |
| cities, states | Read (lookup tables) |
| employee_job_titles, job_titles | Read |
| employee_division, division | Read |

All reads use the read-only `ems_employee` database account, which cannot change any table.

## Error handling

| Situation | What happens |
|---|---|
| Invalid input | Caller is an HR admin: SecurityException; HR uses the editing search (PT-2) instead. The account is not linked to an employee record: an error says the account is not linked. |
| No match found | A co-worker's ID, name or SSN was typed: "No matching employee", and the co-worker's data is never returned. |
| Database error | A DataAccessException is thrown and the screen shows a clear message. Nothing changes, because the search only reads. |

## Acceptance criteria

- [ ] An employee sees their own record, including personal, job and pay details, with the SSN masked.
- [ ] An employee can never see another employee's record, whichever field they search by.
- [ ] A co-worker's ID, name and SSN all give the same "No matching employee" message.
- [ ] The screen has no edit controls, and the database account cannot write.

---

## Test cases

Shared setup for every test: the shared test data (sql/05 to sql/07), freshly loaded, signed in as `sbeagle` / `Employee!2026` (EMPLOYEE), who is employee 1, Snoopy Beagle.

### TC-8.1: Employee views their own record

| Field | Detail |
|---|---|
| **Related PT** | PT-8 (feature 3) |
| **Preconditions / test data** | Signed in as `sbeagle`. Fresh test data. |
| **Steps** | 1. Open the search screen.<br>2. Leave every field empty.<br>3. Click Search. |
| **Input** | No values |
| **Expected result** | One record: Snoopy Beagle, Chief Info. Officer, C-Suite, hired 2022-08-01, salary $45,000.00, date of birth 1985-05-21, mobile 404-555-0101, address 12 Elm St, Atlanta, GA 30303. SSN shown as ***-**-1111. |
| **Actual result** | *(fill in when run)* |
| **Pass / Fail** | *(fill in when run)* |

### TC-8.2: Employee searches for a co-worker by name

| Field | Detail |
|---|---|
| **Related PT** | PT-8 (feature 3) |
| **Preconditions / test data** | Signed in as `sbeagle`. Fresh test data: employee 2 (Charlie Brown) exists. |
| **Steps** | 1. Open the search screen.<br>2. Type the name.<br>3. Click Search. |
| **Input** | Name = "brown" |
| **Expected result** | The message "No matching employee". No details for Charlie Brown are shown. |
| **Actual result** | *(fill in when run)* |
| **Pass / Fail** | *(fill in when run)* |

### TC-8.3: Employee searches for a co-worker by SSN

| Field | Detail |
|---|---|
| **Related PT** | PT-8 (feature 3) |
| **Preconditions / test data** | Signed in as `sbeagle`. Fresh test data: SSN 111-33-1111 belongs to Lucy Doctor (employee 3). |
| **Steps** | 1. Open the search screen.<br>2. Type the SSN.<br>3. Click Search. |
| **Input** | SSN = "111-33-1111" |
| **Expected result** | The message "No matching employee", the same message as TC-8.2. Nothing about Lucy Doctor is shown. |
| **Actual result** | *(fill in when run)* |
| **Pass / Fail** | *(fill in when run)* |
