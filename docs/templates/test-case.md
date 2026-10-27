# Test case template

Copy one block per test case. Use the shared test data (`sql/05` to `sql/07`).

---

### TC-<PT number>.<test number>: <Short title>

| Field | Detail |
|---|---|
| **Related PT** | PT-<n> |
| **Preconditions / test data** | e.g. signed in as `hradmin`; employee 2 has salary $48,000.00 |
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
| **Preconditions / test data** | Signed in as `hradmin`. Fresh test data: employee 2 salary = $48,000.00, employee 1 salary = $45,000.00 |
| **Steps** | 1. Open "Salary Increase".<br>2. Enter percentage and threshold.<br>3. Click Preview, then Apply. |
| **Input** | Percentage = 3.0, Threshold = 48000.00 |
| **Expected result** | Message reports 3 employees updated (1, 5, 6). Employee 1 salary = $46,350.00. Employee 2 salary unchanged at $48,000.00. One audit_log row per updated employee. |
| **Actual result** | |
| **Pass / Fail** | |
