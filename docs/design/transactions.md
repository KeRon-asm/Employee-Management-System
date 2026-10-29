# Transactions: saving a change and its audit row together

Read this before writing any code that changes the database. It affects Members 1, 4, 5 and 6.

## The problem

Every change the app makes also writes a row to `audit_log`. Until now, each DAO opened its own database connection, so the change and its audit row were two separate saves:

* If the change was saved and the audit row then failed:
  * The change stayed in the database
  * Nothing recorded who made it
* If an HR admin deleted the employee record linked to their own login:
  * The delete also removed their login (ON DELETE CASCADE)
  * The audit row written afterward pointed at a login that no longer existed, so MySQL refused it

The fix is to run the change and its audit row on one connection, in one transaction. Either both are saved or neither is.

## The design

There are three small classes:

* `TransactionManager` (package `dao`), which services use
  * One method: `inTransaction(() -> { ... })`
  * Everything inside the block is saved together, or not at all
  * It uses no `java.sql` types, so services stay independent of JDBC
* `ConnectionProvider` (package `dao.jdbc`), which DAOs use instead of `ConnectionFactory`
  * `open(account)` inside a transaction returns the transaction's connection
  * `open(account)` outside a transaction opens a new connection, as before
* `JdbcTransactionManager` (package `dao.jdbc`), which connects the two
  * Built with the `ConnectionProvider` and an account (`Account.HR` for HR work)
  * Starts the transaction, commits it or rolls it back

## How a transaction behaves

When the block finishes:

* It returns normally: everything is committed
* It throws any exception: everything is rolled back, and the same exception reaches the caller unchanged
* The commit itself fails: everything is rolled back and a `DataAccessException` ("nothing was saved") is thrown

Inside the block:

* Every DAO call uses the same connection
  * The account is the transaction's account, whatever account the DAO was built with
  * `ems_hr` can write `audit_log`, so the audit DAO works inside an HR transaction
* A DAO's `try (Connection conn = connections.open(account))` still works
  * Closing that connection does nothing; the transaction closes it at the end
  * Calling `commit()`, `rollback()` or `setAutoCommit()` throws an error, so no DAO can end the transaction early
* A transaction started inside another one joins it
  * Only the outermost block commits
  * If the inner block throws, the whole transaction rolls back

One transaction belongs to one thread:

* Start and finish it on the same thread
* With Swing, run the whole service call inside one `SwingWorker`, not across several

## Setting it up

Create one `ConnectionProvider` for the whole app and share it. Every DAO and transaction manager must use the same one, or they won't share connections.

```java
ConnectionProvider connections = new ConnectionProvider(new ConnectionFactory(config));

TransactionManager hrTransactions = new JdbcTransactionManager(connections, Account.HR);
EmployeeDao employeeDao = new JdbcEmployeeDao(connections, Account.HR);
AuditDao auditDao = new JdbcAuditDao(connections, Account.AUTH);   // Member 1

EmployeeService employees = new EmployeeServiceImpl(employeeDao, auditDao, hrTransactions);
```

## Rules for each member

Everyone writing a `Jdbc...Dao` class:

* Take a `ConnectionProvider` in the constructor, not a `ConnectionFactory`
* Open connections with `try (Connection conn = connections.open(account))`, as `JdbcEmployeeDao.search` does
* Never call `setAutoCommit`, `commit` or `rollback` yourself

Members 4, 5 and 6 (service methods that change data):

* Put the change and its audit row inside one `transactions.inTransaction` block
* Write the audit row first, then make the change
  * The audit row then exists before a delete can remove the login it points to
  * If the change fails, the audit row rolls back with it
* Don't catch an exception inside the block and carry on
  * The transaction would commit whatever had already run

Member 4 (`insert` and `update` in `JdbcEmployeeDao`, several statements each):

* Wrap the statements in `connections.inTransaction(account, () -> { ... })` inside the DAO method
  * The method is then safe even if someone calls it outside a service
  * When the service already started a transaction, this joins it
  * The block can't throw `SQLException`, so catch it inside and rethrow it as `DataAccessException`:

```java
return connections.inTransaction(account, () -> {
    try (Connection conn = connections.open(account)) {
        // several PreparedStatements: employees, addresses, demographics, ...
        return newEmpId;
    } catch (SQLException e) {
        throw new DataAccessException("Could not add the employee", e);
    }
});
```

Member 1 (`JdbcAuditDao` and the login lockout):

* Build `JdbcAuditDao` with `Account.AUTH`
  * Outside a transaction (logins), it uses `ems_auth`, which may insert into `audit_log`
  * Inside an HR transaction, it joins the HR connection automatically
* Use a `JdbcTransactionManager` with `Account.AUTH` to save the failed-attempt count and the `LOGIN_FAILED` row together

## Example: deleting an employee (Member 5)

```java
@Override
public boolean deleteEmployee(Session session, int empId) {
    requireRole(session, Role.HR_ADMIN);
    return transactions.inTransaction(() -> {
        Optional<Employee> existing = employeeDao.findById(empId);
        if (existing.isEmpty()) {
            return false;                                   // no match: nothing saved
        }
        auditDao.record(session.userId(), AuditDao.Action.DELETE, "employees",
                String.valueOf(empId), describe(existing.get()), null);   // audit row first
                // describe(...) is your own helper: a text snapshot of the employee, SSN masked
        if (!employeeDao.delete(empId)) {
            throw new IllegalStateException("Employee " + empId + " was deleted by someone else");
        }
        return true;                                        // both saved together
    });
}
```

What happens in each case:

* The employee exists: the audit row and the delete are committed together
* The empID doesn't exist: nothing is written and the method returns false
* The delete fails (for example, the database goes down): the audit row is rolled back too
* The deleted employee is the admin's own login:
  * The delete succeeds
  * MySQL sets that audit row's `user_id` to empty (ON DELETE SET NULL), so the row survives but no longer names who did it

## Testing status

* `ConnectionProviderTest` checks commit, rollback, joining, closing and the one-thread rule with fake connections
* None of this has run against a real MySQL server yet
* Transactions need InnoDB tables. MySQL 8.4 (our Docker image) uses InnoDB by default, and the professor's scripts don't choose another engine
