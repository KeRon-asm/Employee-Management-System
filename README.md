# Company Z Employee Management System

A secure, role-based employee management system for Company Z, built in Java and MySQL for CSc 3350 (Fall 2026).

Company Z's HR admin currently manages ~75 employees' records by hand in DBeaver using the MySQL **root** account, and employees can only see their pay stubs as PDFs. This system replaces that with:

- **Role-based login.** HR admins get full CRUD; employees see only their own pay and demographic data.
- **Real security.** Salted PBKDF2 password hashing, account lockout, SSN masking, and separate least-privilege MySQL accounts instead of root.
- **Audit trail.** Every insert, update, delete and login is recorded with who, when, and old and new values.
- **Safe salary raises.** Percentage raises by salary range run in a single transaction, with a preview before committing.
- **Payroll reports.** Pay history, monthly totals by job title and division, new hires by date range, and staff grouped by job title.
- **Built to scale.** Indexed search on name, DOB, SSN and empID, ready for the planned growth to ~225 employees.

**Constraints:** plain Java (17+), Swing or JavaFX, MySQL. No frameworks (no Spring, no web stack). The only libraries are the MySQL JDBC driver and JUnit for tests.

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

---

## Architecture

```
ui  ──►  service  ──►  dao (interfaces)  ──►  dao.jdbc (MySQL)
             │
             └──►  security (hashing, masking)       model = plain data objects
```

| Package | Responsibility |
|---|---|
| `model` | Plain data: `Employee`, `Address`, `Demographics`, `PayStatement`, `UserAccount`, `Role`, `SearchCriteria` |
| `dao` | Interfaces for data access: `EmployeeDao`, `PayrollDao`, `UserDao`, `AuditDao` |
| `dao.jdbc` | MySQL implementations. PreparedStatement only, try-with-resources, transactions for multi-step changes |
| `service` | Business rules: login and lockout, role checks, validation, audit logging |
| `report` | Abstract `Report` base class; one subclass per report (6a-6e) |
| `security` | `PasswordHasher` (PBKDF2), `SsnMasker` |
| `config` | `db.properties` loading and per-role database connections |
| `ui` | Screens only; no SQL or business logic |

**Rule of thumb:** the UI calls services, services call DAO interfaces, and nothing above `dao.jdbc` imports `java.sql`.

### Database

| Script | What it does |
|---|---|
| `sql/01_base_schema.sql` | Original employeeData tables (**reconcile with the professor's script**) |
| `sql/02_normalization.sql` | 3NF tables: `states`, `cities`, `addresses`, `demographics`, plus search indexes |
| `sql/03_security.sql` | `app_users` (hashed logins) and `audit_log` |
| `sql/04_app_users.sql` | Least-privilege MySQL accounts: `ems_auth`, `ems_employee`, `ems_hr` |
| `sql/05_seed_data.sql` | Shared test data. **Write all test cases against these rows** |

---

## Project status

| Area | Status |
|---|---|
| Project structure, build, CI | Done |
| Database schema, security accounts, seed data | Done (base schema needs checking against the professor's script) |
| Password hashing, SSN masking (with tests) | Done |
| Domain model, DAO interfaces, report base class | Done |
| JDBC DAO implementations | To do |
| Services (auth, employee, salary, reports) | To do |
| UI | To do (Swing vs. JavaFX decision pending) |

## Deliverables

| Due | Deliverable | Where |
|---|---|---|
| 11/1 | Programming tasks and test cases (group) | `docs/` (see [team plan](docs/team-plan.md)) |
| 11/22 | Sequence diagrams (individual) | Individual |
| 12/6 | Software design document (group) | `docs/` |
| 12/6 | Source ZIP and video demo (group) | This repo |

See [CONTRIBUTING.md](CONTRIBUTING.md) for how we work.

## Troubleshooting

- **`Missing db.properties`.** Run `cp db.properties.example db.properties`.
- **`Access denied for user 'ems_...'`.** The database was created before `04_app_users.sql` existed. Run `docker compose down -v && docker compose up -d`.
- **`Communications link failure`.** MySQL isn't running yet. Wait about 20 seconds after `docker compose up`, or check `docker compose logs mysql`.
- **Port 3306 already in use.** A local MySQL is already running. Stop it, or change the left side of the port mapping in `docker-compose.yml` (for example `"3307:3306"`) and in `db.url`.
