# Deliverable 2: Programming tasks and test cases

**Due: Sunday 11/1/2026, 11:59 PM. No late submissions. One person uploads.**

The assignment asks for 5 programming tasks (PTs) from the user story, plus tasks (a), (b) and (c) each written as a detailed PT with pass/fail test cases (at least one each). Each of us owns one PT and its test cases.

| Member | Programming task | Test cases | Extra duty |
|---|---|---|---|
| 1 | PT-1: Login and role-based authorization (feature 1) | 2+ (valid login, wrong password, employee blocked from admin screens) | **Editor:** templates, formatting, final document, submission |
| 2 | PT-2: Employee search by name, DOB, SSN, empID. Edit mode for HR, view mode for employees (features 2-3) | 2+ | **Shared test data:** owns `sql/05_seed_data.sql` |
| 3 | PT-3: Reports 6a-6e (feature 6) | 2+ | Checks every PT names the correct tables, including the new 3NF tables |
| 4 | PT-4 / task (a): Update employee data, written generally using our model classes | **3+** | |
| 5 | PT-5 / task (b): Search by empID for deletion. Confirm if found, "no match" otherwise | **3+** (found and confirmed, found and cancelled, not found) | |
| 6 | PT-6 / task (c): Raise salary by a percentage when salary is below an amount (also covers feature 5's range version) | **3+** | Explains "less than X" vs. "within a range" |

**Fill in names:** Member 1 = ___, 2 = ___, 3 = ___, 4 = ___, 5 = ___, 6 = ___

## How to write your section

1. Copy `docs/templates/programming-task.md` to `docs/deliverable-2/PT-<n>.md` and fill it in.
2. Write your test cases with `docs/templates/test-case.md`, in the same file.
3. Use the **shared test data** in `sql/05_seed_data.sql` (empids 1001-1006, and 9999 for "not found"). Don't invent new employees.
4. Open a pull request. Your assigned reviewer approves it.

Good test cases cover the edges. For (c) with a $60,000 threshold: empid 1003 earns exactly $60,000 and must **not** change; 1002 earns $59,999.99 and **must** change; a negative percentage must be rejected; and a threshold nobody is under must report 0 updated.

## Timeline

| Date | Milestone |
|---|---|
| Wed 10/28 | Templates and seed data final (Members 1 and 2) |
| Thu 10/29 | Every PT and its test cases drafted, pull request opened |
| Fri 10/30 | Peer review: 1 reviews 2, 2 reviews 3, 3 reviews 4, 4 reviews 5, 5 reviews 6, 6 reviews 1 |
| Sat 10/31 | Member 1 assembles one PDF and shares it for a last look |
| Sun 11/1 | Member 1 submits **by 6 PM**. Nobody else uploads |
