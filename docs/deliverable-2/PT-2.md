# PT-2: HR admin searches for employees to edit

## Overview

This task covers user story feature 2: searching for an employee by name, date of birth, SSN or employee ID so their information can be edited. Only an HR admin can use it, because the results include SSNs and dates of birth. The HR admin types one or more search values, the system shows every matching employee in a list, and picking a row opens that employee for editing (PT-4). Written by Member 2.

## Inputs

The HR admin can fill in any of these fields:

* Employee ID
  * A whole number
  * Must match exactly
* Name
  * Matches part of a first name, last name, or full name
  * Not case-sensitive (typing "brown" finds Charlie Brown)
* Date of birth
  * Typed as YYYY-MM-DD
  * Must match exactly
* SSN
  * Must match exactly
  * Can be typed with or without dashes (111-33-1111 or 111331111)

At least one field is required. If more than one is filled in, an employee must match all of them.

## Outputs

Match found:

* A list of employees
  * Sorted by last name, then first name
  * Showing these details:
    * Employee ID
    * Name
    * Job title
    * Division
    * Hire date
  * Picking a row opens that employee for editing (PT-4)

No match:

* The message "No matching employee"
* An empty list

Nothing in the database changes, because this task only reads data.

## Preconditions

Before this task runs:

* The user is signed in as an HR admin (PT-1)
* The database is running with the shared test data loaded

## How it works

The search happens in four stages:

* On the search screen
  * The HR admin enters values and clicks Search
  * The screen puts the values in a SearchCriteria object and calls EmployeeService.searchForEditing
* In the service (EmployeeServiceImpl)
  * Checks that the user is an HR admin; if not, it throws a SecurityException and stops
  * Checks that at least one field was filled in; if not, it rejects the search with "Enter at least one search value: name, date of birth, SSN or employee ID"
  * Calls EmployeeDao.search
* In the database layer (JdbcEmployeeDao)
  * Builds one SELECT joining employees, demographics, addresses, cities, states, job titles and divisions, with one condition for each field that was given
  * Binds every typed value as a ? placeholder in a PreparedStatement, so typed text can never run as SQL
  * Orders the results by last name, first name, then employee ID
  * Turns each row into an Employee object (with its Address and Demographics)
* Back on the search screen
  * Shows the list, or "No matching employee" if the list is empty

## Classes and methods

* Screen: the search screen (still to be built)
* Service: EmployeeServiceImpl
  * searchForEditing(Session, SearchCriteria)
* Data access
  * Interface EmployeeDao: search(SearchCriteria) and findById(int)
  * Implementation JdbcEmployeeDao: search and findById, plus the helpers buildSearchSql, normalizeSsn, escapeLike and mapEmployee
* Model classes: SearchCriteria, Employee, Address, Demographics, Session, Role

## Database tables

This task only reads data, using the ems_hr database account. It reads:

* Employee data: employees, demographics, addresses
* Lookup tables: cities, states
* Job and division: employee_job_titles, job_titles, employee_division, division

## Error handling

The search is refused before it touches the database when:

* The user is not an HR admin: a SecurityException is thrown
* Nothing was entered: the user sees "Enter at least one search value..."

When the search runs:

* No employee matches: the user sees "No matching employee" and an empty list
* The text typed contains %, _ or SQL such as '; DROP TABLE: it is treated as plain text to look for and never runs as SQL
* The database cannot be reached: a DataAccessException ("Could not search employees") is thrown and the screen shows a clear message

## Acceptance criteria

This task is done when:

* An HR admin can search by name, date of birth, SSN or employee ID, alone or combined
* A name search matches part of a name in any letter case, and an SSN search works with or without dashes
* A non-HR user cannot run the search, and a search with nothing entered is refused
* No match shows "No matching employee", and all three test cases below pass

## Test cases

All test cases use the shared test data (sql/05, 06 and 07), freshly loaded, and are run while signed in as hradmin (password HrAdmin!2026).

### TC-2.1: Search by name finds one employee

Steps:

* Open the employee search screen
* Type "brown" in the Name field and leave the other fields empty
* Click Search

Expected result:

* Exactly one result: employee 2
  * Charlie Brown
  * Chief Exec. Officer, Systems
  * Hired 2022-07-01

Actual result: (fill in when run)

Pass / Fail: (fill in when run)

### TC-2.2: Search by SSN finds one employee

Steps:

* Open the employee search screen
* Type "111-33-1111" in the SSN field and leave the other fields empty
* Click Search

Expected result:

* Exactly one result: employee 3
  * Lucy Doctor
  * Chief Finn. Officer, C-Suite
  * Hired 2022-07-03
* Searching again with "111331111" (no dashes) gives the same result

Actual result: (fill in when run)

Pass / Fail: (fill in when run)

### TC-2.3: Search for an employee that does not exist

Setup: employee ID 9999 does not exist in the test data.

Steps:

* Open the employee search screen
* Type "9999" in the Employee ID field and leave the other fields empty
* Click Search

Expected result:

* The message "No matching employee"
* An empty results list
* Nothing in the database changes

Actual result: (fill in when run)

Pass / Fail: (fill in when run)
