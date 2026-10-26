# Test case template

Copy one block per test case. Use the shared data in `sql/05_seed_data.sql`.

---

### TC-<PT number>.<test number>: <Short title>

| Field | Detail |
|---|---|
| **Related PT** | PT-<n> |
| **Preconditions / test data** | e.g. signed in as `hradmin`; empid 1003 has salary $60,000.00 |
| **Steps** | 1. ...<br>2. ...<br>3. ... |
| **Input** | Exact values entered |
| **Expected result** | Exact screen message and exact database values afterward |
| **Actual result** | *(fill in when run)* |
| **Pass / Fail** | *(fill in when run)* |

---

## Example

### TC-6.2: Employee exactly at the threshold is not raised

| Field | Detail |
|---|---|
| **Related PT** | PT-6 (task c) |
| **Preconditions / test data** | Signed in as `hradmin`. Fresh seed data: empid 1003 salary = $60,000.00, empid 1002 salary = $59,999.99 |
| **Steps** | 1. Open "Salary Increase".<br>2. Enter percentage and threshold.<br>3. Click Preview, then Apply. |
| **Input** | Percentage = 3.0, Threshold = 60000.00 |
| **Expected result** | Message reports 2 employees updated (1001, 1002). Empid 1002 salary = $61,799.99. Empid 1003 salary unchanged at $60,000.00. One audit_log row per updated employee. |
| **Actual result** | |
| **Pass / Fail** | |
