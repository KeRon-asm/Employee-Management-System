# PT-8: Employee views their own record

## Overview

This is an extra task (not required for 11/1), and its test cases are included anyway. It covers user story feature 3 (searching for an employee to view their information, general employees only) and feature 1b (employees see their own pay and demographic data). A signed-in employee can look up their own information, but only their own: nobody can see a co-worker's SSN, date of birth or pay. The screen is read-only, and the SSN is always masked. Written by Member 2.

## Inputs

The employee can fill in the same four fields as PT-2 (employee ID, name, date of birth and SSN), and all of them are optional. Whatever is typed must match the employee's own record. Leaving every field empty simply shows their own record.

## Outputs

Their own record matches:

* A read-only view of the employee's record
  * Personal details
    * Name and email
    * Date of birth and mobile phone
    * Emergency contact
    * Address
  * Job details
    * Job title and division
    * Hire date
    * Salary
  * The SSN, masked as ***-**-NNNN (the last four digits only)

Anything else (a co-worker's ID, name or SSN):

* The message "No matching employee"
* The same message every time, so the search cannot be used to find out who works at the company

Nothing in the database changes, because this task only reads data.

## Preconditions

Before this task runs:

* The user is signed in as an employee (PT-1), and the account is linked to an employee record
* The database is running with the shared test data loaded

## How it works

The search happens in four stages:

* On the search screen
  * The employee enters values (or none) and clicks Search
  * The screen calls EmployeeService.searchForViewing
* In the service (EmployeeServiceImpl)
  * Checks that the user is an employee; if not, it throws a SecurityException
  * If the employee typed an employee ID that is not their own, it returns "no match" straight away without calling the database
  * Otherwise it replaces the employee ID with the signed-in employee's own ID, keeps whatever else was typed, and calls EmployeeDao.search
  * If a row comes back, it returns a copy with the SSN masked by SsnMasker; the stored record is unchanged
* In the database layer (JdbcEmployeeDao)
  * Runs the same query as PT-2, using the read-only ems_employee account
* Back on the search screen
  * Shows the record, or "No matching employee"

## Classes and methods

* Screen: the search screen in view mode (still to be built)
* Service: EmployeeServiceImpl
  * searchForViewing(Session, SearchCriteria)
* Data access: JdbcEmployeeDao
  * search(SearchCriteria), the same method PT-2 uses
* Security: SsnMasker
  * mask(String)
* Model classes: SearchCriteria, Employee, Session

## Database tables

This task only reads data, using the read-only ems_employee database account. It reads:

* Employee data: employees, demographics, addresses
* Lookup tables: cities, states
* Job and division: employee_job_titles, job_titles, employee_division, division

## Error handling

When the search is refused or finds nothing:

* The user is an HR admin: a SecurityException is thrown, and HR uses the editing search (PT-2) instead
* The employee types a co-worker's ID, name or SSN: the user sees "No matching employee" and the co-worker's data is never returned

When something is wrong with the account or the database:

* The account is not linked to an employee record: an error says the account is not linked
* The database cannot be reached: a DataAccessException is thrown and the screen shows a clear message

## Acceptance criteria

This task is done when:

* An employee sees their own record, including personal, job and pay details, with the SSN masked
* An employee can never see another employee's record, whichever field they search by
* A co-worker's ID, name and SSN all give the same "No matching employee" message
* The screen has no edit controls, and the database account cannot write

## Test cases

All test cases use the shared test data (sql/05, 06 and 07), freshly loaded, and are run while signed in as sbeagle (password Employee!2026), who is employee 1, Snoopy Beagle.

### TC-8.1: Employee views their own record

Steps:

* Open the search screen
* Leave every field empty
* Click Search

Expected result:

* One record: Snoopy Beagle
  * Chief Info. Officer, C-Suite
  * Hired 2022-08-01, salary $45,000.00
  * Date of birth 1985-05-21, mobile 404-555-0101
  * Address: 12 Elm St, Atlanta, GA 30303
  * SSN shown as ***-**-1111

Actual result: (fill in when run)

Pass / Fail: (fill in when run)

### TC-8.2: Employee searches for a co-worker by name

Setup: employee 2 (Charlie Brown) exists in the test data.

Steps:

* Open the search screen
* Type "brown" in the Name field
* Click Search

Expected result:

* The message "No matching employee"
* No details for Charlie Brown are shown

Actual result: (fill in when run)

Pass / Fail: (fill in when run)

### TC-8.3: Employee searches for a co-worker by SSN

Setup: SSN 111-33-1111 belongs to Lucy Doctor (employee 3).

Steps:

* Open the search screen
* Type "111-33-1111" in the SSN field
* Click Search

Expected result:

* The message "No matching employee", the same message as TC-8.2
* Nothing about Lucy Doctor is shown

Actual result: (fill in when run)

Pass / Fail: (fill in when run)
