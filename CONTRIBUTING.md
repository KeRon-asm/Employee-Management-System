# How we work

## Branches and pull requests

- `main` always builds and passes tests. Nobody pushes to it directly.
- Make a branch for each piece of work: `feature/login`, `feature/salary-raise`, `docs/pt-search`, `fix/search-null-dob`.
- Open a pull request when you're ready. **One teammate reviews and approves** before merging.
- Keep pull requests small. One feature or one document section each.

```bash
git checkout main && git pull
git checkout -b feature/your-thing
# ...work, commit...
git push -u origin feature/your-thing
# then open a pull request on GitHub
```

## Commit messages

Start with a short verb phrase describing what the commit does:

```
Add JdbcEmployeeDao search by name and DOB
Fix salary raise including employees exactly at the threshold
Write PT-4 (update employee) and test cases TC-4.1 to TC-4.3
```

## Code rules

These are worth points (cohesion, coupling, interfaces, abstract classes) and keep us out of each other's way.

1. **Layers.** UI → service → DAO interface → JDBC implementation. The UI never runs SQL.
2. **PreparedStatement only.** Never build SQL by joining strings. That's how SQL injection happens.
3. **try-with-resources** for every `Connection`, `Statement` and `ResultSet`.
4. **BigDecimal for money.** Never `double`.
5. **Never log or print an SSN or password.** Use `SsnMasker` for display.
6. **Audit every change, in the same transaction.** Inserts, updates and deletes write an `AuditDao` row first, then make the change, inside one `TransactionManager.inTransaction` block. DAOs get connections from the shared `ConnectionProvider` and never call `commit` or `rollback`. See [docs/design/transactions.md](docs/design/transactions.md).
7. **One class, one job.** If a file is getting long, split it.
8. **Secrets stay local.** `db.properties` is git-ignored. Don't commit passwords.

## Tests

- `mvn verify` must pass before you open a pull request. GitHub Actions runs it on every push too.
- Add unit tests next to the code you write (`src/test/java/...`, same package).
- Written test cases for the documents use the shared test data (`sql/05` to `sql/07`) and the template in `docs/templates/test-case.md`.

## Libraries

Only the MySQL driver and JUnit. **Ask the group (and confirm with the professor) before adding anything else.** The assignment bans frameworks.
