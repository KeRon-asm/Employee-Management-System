# Company Z Employee Management System

A secure, role-based employee management system for Company Z, built in Java and MySQL
## The problem

Company Z has about 75 full-time employees and plans to triple that within 24 months. Today:

- One HR admin maintains every record by hand in DBeaver with raw MySQL scripts.
- That admin connects as the MySQL **root** user. There is no other security.
- Employees can't look anything up. They get a PDF pay stub, and a W-2 at the end of the tax year.

## What we're building

| # | User story feature | Who | How we deliver it |
|---|---|---|---|
| 1 | Logon and authorization | Everyone | Hashed passwords (PBKDF2), lockout after 5 failures, audit-logged logins |
| 1a | Secure CRUD on the entire database | HR admin | Full add / edit / delete through the `ems_hr` MySQL account. Root is never used |
| 1b | See own pay and demographic data | Employee | Read-only `ems_employee` account; employees only ever see their own record |
| 2 | Search by name, DOB, SSN or empID **to edit** | HR admin | Combined search on indexed columns |
| 3 | Search by name, DOB, SSN or empID **to view** | Employee | Same search, read-only, own record only, SSN masked |
| 4 | Update an employee picked from search results | HR admin | Every table in one transaction, with old and new values audit-logged |
| 5 | Percentage raise for salaries in a range | HR admin | Preview who's affected, then apply in one transaction |
| 6a | Pay statement history, newest first | Employee | Replaces the PDF-only pay stub |
| 6b | Total pay for a month by job title | HR admin | With employee counts and a grand total |
| 6c | Total pay for a month by division | HR admin | With employee counts and a grand total |
| 6d | New hires within a date range | Everyone | Directory fields only. No salary or SSN |
| 6e | All employees grouped by job title | HR admin | For management |
| 7 | MySQL, local or Aiven | — | Docker for local; [Aiven instructions below](#using-aiven-instead-of-local-mysql) |

Every requirement is traced to its classes, tables and database account in **[docs/requirements.md](docs/requirements.md)**, along with the design assumptions we made.

**Constraints:** plain Java (17+), Swing or JavaFX, MySQL. No frameworks, no web stack. The only libraries are the MySQL JDBC driver and JUnit for tests.

---

## Quick start

**Prerequisites:** JDK 17 or newer, Maven, and Docker Desktop (or your own MySQL 8 server).

```bash
# 1. Start MySQL with the schema and shared test data already loaded
docker compose up -d

# 2. Create your local config (git-ignored -- never commit passwords)
cp db.properties.example db.properties

# 3. Build and run the tests
mvn verify

# 4. Check that all three database accounts can connect
mvn -q compile exec:java
```

If step 4 prints `All accounts connected`, you're set up.

**No Docker?** Run the files in `sql/` in numeric order in MySQL Workbench or DBeaver, as root, then continue from step 2.

**Starting the database over:** `docker compose down -v && docker compose up -d`

### Demo logins (local development only)

| Username | Password | Role |
|---|---|---|
| `hradmin` | `HrAdmin!2026` | HR_ADMIN |
| `abrooks` | `Employee!2026` | EMPLOYEE (empid 1001) |

### Using Aiven instead of local MySQL

1. Create a MySQL service on [Aiven](https://aiven.io) and note its host, port and `avnadmin` password.
2. Connect to it in DBeaver as `avnadmin`. Run `sql/01` through `sql/05` in order.
   **First, change the three passwords in `sql/04_app_users.sql`.** A hosted database is reachable from the internet.
3. In `db.properties`, set the URL (Aiven requires SSL) and the new passwords:
   ```
   db.url=jdbc:mysql://<host>:<port>/employeeData?sslMode=REQUIRED
   ```
4. Share the passwords with the team privately, never in the repo or a group chat screenshot.

---

## Architecture

```
ui  ──►  service  ──►  dao (interfaces)  ──►  dao.jdbc (MySQL)
 │           │
 └──► report ┘          security (hashing, masking)       model (plain data)
```

| Package | Responsibility |
|---|---|
| `model` | Plain data: `Employee`, `Address`, `Demographics`, `PayStatement`, `PayTotal`, `UserAccount`, `Session`, `Role`, `SearchCriteria`, `SalaryRaise`, `DateRange` |
| `service` | Features 1-5: `AuthService` (login and lockout) and `EmployeeService` (search, add, update, delete, raises). Role checks and audit logging happen here |
| `report` | Feature 6: abstract `Report`, with `PayHistoryReport` (6a), `MonthlyPayByJobTitleReport` (6b), `MonthlyPayByDivisionReport` (6c), `NewHiresReport` (6d), `EmployeesByJobTitleReport` (6e). 6b and 6c share `MonthlyPayTotalReport` |
| `dao` | Data-access interfaces: `EmployeeDao`, `PayrollDao`, `ReportDao`, `UserDao`, `AuditDao` |
| `dao.jdbc` | MySQL implementations. PreparedStatement only, try-with-resources, transactions for multi-table changes |
| `security` | `PasswordHasher` (PBKDF2), `SsnMasker` |
| `config` | `db.properties` loading and per-role database connections |
| `ui` | Screens only. No SQL or business logic |

**Rules of thumb:**
- The UI calls services and reports. Services call DAO interfaces. Nothing above `dao.jdbc` imports `java.sql`.
- Every service and report method takes the signed-in `Session`, so a screen can't skip a permission check.

### Security levels

| MySQL account | Used when | Can do |
|---|---|---|
| `ems_auth` | Before sign-in | Read and update `app_users`; write `audit_log`. Nothing else |
| `ems_employee` | A general employee is signed in | Read only. The service layer further limits them to their own rows |
| `ems_hr` | An HR admin is signed in | Select, insert, update, delete on all data. Cannot drop or alter tables |

### Database

| Script | What it does |
|---|---|
| `sql/01_base_schema.sql` | Original employeeData tables (**reconcile with the professor's script**) |
| `sql/02_normalization.sql` | 3NF tables: `states`, `cities`, `addresses`, `demographics`, plus search indexes |
| `sql/03_security.sql` | `app_users` (hashed logins) and `audit_log` |
| `sql/04_app_users.sql` | The three least-privilege MySQL accounts above |
| `sql/05_seed_data.sql` | Shared test data with salary boundary cases. **Write all test cases against these rows** |

---

## Project status

| Area | Status |
|---|---|
| Project structure, build, CI | Done |
| Database schema, security accounts, seed data | Done (base schema needs checking against the professor's script) |
| Password hashing, SSN masking | Done, with tests |
| Salary raise rules (range and "less than") | Done, with boundary tests |
| Reports 6a-6e: role checks, sorting, grouping, totals | Done, with tests (need JDBC data access) |
| Service and DAO interfaces | Done |
| JDBC DAO implementations | To do |
| `AuthService` and `EmployeeService` implementations | To do |
| UI | To do (Swing vs. JavaFX decision pending) |

## Deliverables

| Due | Deliverable | Where |
|---|---|---|
| 11/1 | Programming tasks and test cases (group) | `docs/` (see [team plan](docs/team-plan.md)) |
| 11/22 | Sequence diagrams (individual) | Individual |
| 12/6 | Software design document (group) | `docs/` (start from [requirements.md](docs/requirements.md)) |
| 12/6 | Source ZIP and video demo (group) | This repo |

See [CONTRIBUTING.md](CONTRIBUTING.md) for how we work.

## Troubleshooting

- **`Missing db.properties`.** Run `cp db.properties.example db.properties`.
- **`Access denied for user 'ems_...'`.** The database was created before `04_app_users.sql` existed. Run `docker compose down -v && docker compose up -d`.
- **`Communications link failure`.** MySQL isn't running yet. Wait about 20 seconds after `docker compose up`, or check `docker compose logs mysql`.
- **Port 3306 already in use.** A local MySQL is already running. Stop it, or change the left side of the port mapping in `docker-compose.yml` (for example `"3307:3306"`) and in `db.url`.
- **SSL errors on Aiven.** Make sure `db.url` ends with `?sslMode=REQUIRED`.
