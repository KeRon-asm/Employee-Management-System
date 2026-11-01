# Programming Task 1 (PT-1): Login and Role-Based Authorization

## 1. Task Overview
* **Feature:** User Logon / Authorization & Security (Feature 1)
* **Target File:** `src/main/java/com/companyz/ems/service/AuthServiceImpl.java`
* **Interface Implemented:** `com.companyz.ems.service.AuthService`
* **Database Account Used:** `ems_auth`
* **Collaborators / Dependencies:**
  * `com.companyz.ems.dao.UserDao` (`JdbcUserDao`)
  * `com.companyz.ems.dao.AuditDao` (`JdbcAuditDao`)
  * `com.companyz.ems.security.PasswordHasher`
  * `com.companyz.ems.model.Session`
  * `com.companyz.ems.model.User`

---

## 2. Detailed Technical Specification

### Method Signature
```java
public Session login(String username, char[] password) throws AuthenticationException
```

### Behavioral Workflow & Business Rules

#### 1. Lookup via `JdbcUserDao`

- Query `app_users` for the record matching `username` using `UserDao.findByUsername(username)`.
- If no user record exists, execute a dummy hash comparison using `PasswordHasher` to prevent timing attacks, write a `LOGIN_FAILED` record to `audit_log` via `AuditDao.record(...)`, and throw an `AuthenticationException` with the generic message: `"Invalid username or password."`

#### 2. Lockout Check

- If the user is found, check `app_users.locked_until`.
- If `locked_until` is non-null and is greater than the current system timestamp (`NOW()`):
  - Write `LOGIN_FAILED` to `audit_log` with an indication of account lockout.
  - Throw an `AuthenticationException` with the message: `"Account locked. Try again in 15 minutes."`
- If `locked_until` has passed, clear the lockout and reset `failed_attempts` before evaluating the credentials.

#### 3. Password Verification

- Compare `password` against the stored password hash using `PasswordHasher.verify(password, user.getPasswordHash())`.
- Overwrite and zero-fill the `char[] password` array immediately after verification to purge raw credentials from memory.

#### 4. Failure Branch (Incorrect Password)

- Increment `app_users.failed_attempts` by calling `UserDao.recordFailedAttempt(username)`.
- If `failed_attempts` reaches 5:
  - Set `locked_until = NOW() + INTERVAL 15 MINUTE`.
  - Write `LOGIN_FAILED` to `audit_log` with a note specifying account lockout.
  - Throw `AuthenticationException("Account locked. Try again in 15 minutes.")`.
- If `failed_attempts` < 5:
  - Write `LOGIN_FAILED` to `audit_log`.
  - Throw `AuthenticationException("Invalid username or password.")`

#### 5. Success Branch (Valid Password)

- Reset `failed_attempts` to 0 and set `locked_until = NULL` via `UserDao.resetFailedAttempts(username)`.
- Insert a `LOGIN` action record into `audit_log` using `AuditDao.record(...)`.
- Construct and return an authenticated `Session` object containing:
  - `empID` (e.g., `1` for `sbeagle`, `0`/admin ID for `hradmin`)
  - `username`
  - Granted role (`Role.HR_ADMIN` or `Role.EMPLOYEE`)
  - Generated session token and authentication timestamp.

---

## 3. Test Cases

### TC-1.1: Successful HR Admin Authentication

**Objective:** Verify valid credentials generate an active `HR_ADMIN` session and record a successful login in `audit_log`.

**Preconditions:**
- Test data initialized using `sql/05`, `06`, and `07`.
- User `hradmin` exists, account is not locked, and role is `HR_ADMIN`.

**Input Steps:**
1. Call `authService.login("hradmin", "HrAdmin!2026".toCharArray())`.

**Expected Results:**
- Login succeeds without throwing an exception.
- Returned `Session` object is non-null.
- `session.getRole()` returns `Role.HR_ADMIN`.
- `app_users.failed_attempts` for `hradmin` remains `0`.
- Exactly one row is inserted into `audit_log` with action `LOGIN`.

**Pass/Fail Criteria:** Pass if a valid `Session` with `HR_ADMIN` role is returned and a `LOGIN` row appears in `audit_log`; Fail if an exception is thrown or no audit entry is created.

### TC-1.2: Failed Authentication & Attempt Counter Increment

**Objective:** Verify that an invalid password displays the generic error message and increments `failed_attempts` without locking.

**Preconditions:**
- User `sbeagle` exists (empID 1, Snoopy Beagle) with `failed_attempts = 0` and is not locked.

**Input Steps:**
1. Call `authService.login("sbeagle", "wrongpass".toCharArray())`.

**Expected Results:**
- Throws `AuthenticationException` with the exact message: `"Invalid username or password."`
- No `Session` object is created.
- In `app_users`, `failed_attempts` for `sbeagle` is incremented to `1`.
- Exactly one row is inserted into `audit_log` with action `LOGIN_FAILED`.

**Pass/Fail Criteria:** Pass if the generic error message is thrown, `failed_attempts` is updated to 1, and `LOGIN_FAILED` is logged; Fail if the message leaks account existence or attempts are not tracked.

### TC-1.3: Account Lockout on 5 Consecutive Failed Attempts

**Objective:** Verify that 5 consecutive failed attempts trigger a 15-minute lockout and reject correct credentials during the lockout period.

**Preconditions:**
- User `sbeagle` exists with `failed_attempts = 4` and `locked_until` is null.

**Input Steps:**
1. Attempt 5th invalid login: `authService.login("sbeagle", "wrongpass".toCharArray())`.
2. Immediately attempt valid login: `authService.login("sbeagle", "Employee!2026".toCharArray())`.

**Expected Results:**
- Step 1 throws `AuthenticationException` with the message: `"Account locked. Try again in 15 minutes."`
- In `app_users`, `failed_attempts` becomes `5` and `locked_until` is set to 15 minutes in the future.
- Step 2 with the correct password ("Employee!2026") is refused immediately.
- Step 2 throws `AuthenticationException` with the message: `"Account locked. Try again in 15 minutes."`
- No active session is returned for either attempt.

**Pass/Fail Criteria:** Pass if the 5th attempt triggers lockout and subsequent valid login attempts are rejected during the 15-minute window; Fail if the correct password authenticates while locked.
