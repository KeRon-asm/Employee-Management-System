package com.companyz.ems.dao.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.companyz.ems.config.DatabaseConfig.Account;
import com.companyz.ems.dao.DataAccessException;
import com.companyz.ems.dao.TransactionManager;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

/**
 * Transaction rules from docs/design/transactions.md, checked with fake connections that
 * record every call. No database needed.
 */
class ConnectionProviderTest {

    private final FakeDatabase db = new FakeDatabase();
    private final ConnectionProvider connections = new ConnectionProvider(db::open);

    // ---- commit and rollback ----

    @Test
    void successfulWorkIsCommittedOnce() {
        String result = connections.inTransaction(Account.HR, () -> "done");

        assertEquals("done", result);
        assertEquals(List.of(Account.HR), db.opened);
        assertEquals(List.of("c1.setAutoCommit(false)", "c1.commit", "c1.close"), db.calls);
    }

    @Test
    void failedWorkIsRolledBackAndTheSameExceptionReachesTheCaller() {
        IllegalStateException thrown = new IllegalStateException("delete failed");

        IllegalStateException caught = assertThrows(IllegalStateException.class,
                () -> connections.inTransaction(Account.HR, () -> {
                    throw thrown;
                }));

        assertTrue(caught == thrown);
        assertEquals(List.of("c1.setAutoCommit(false)", "c1.rollback", "c1.close"), db.calls);
    }

    @Test
    void failedCommitIsRolledBackAndReported() {
        db.failCommit = true;

        DataAccessException e = assertThrows(DataAccessException.class,
                () -> connections.inTransaction(Account.HR, () -> "done"));

        assertEquals("Could not save changes; nothing was saved", e.getMessage());
        assertEquals(List.of("c1.setAutoCommit(false)", "c1.commit", "c1.rollback", "c1.close"), db.calls);
    }

    @Test
    void unreachableDatabaseIsReportedAndNothingRuns() {
        db.failOpen = true;
        List<String> ran = new ArrayList<>();

        assertThrows(DataAccessException.class,
                () -> connections.inTransaction(Account.HR, () -> ran.add("work")));

        assertTrue(ran.isEmpty());
        assertFalse(connections.isInTransaction());
    }

    // ---- DAOs inside a transaction share one connection ----

    @Test
    void everyDaoCallInsideATransactionUsesTheSameConnection() {
        connections.inTransaction(Account.HR, () -> {
            useLikeADao(Account.HR);        // e.g. auditDao.record
            useLikeADao(Account.AUTH);      // a DAO built for another account still joins
            useLikeADao(Account.HR);        // e.g. employeeDao.delete
            return null;
        });

        assertEquals(List.of(Account.HR), db.opened);
        assertEquals(List.of("c1.setAutoCommit(false)",
                "c1.isValid", "c1.isValid", "c1.isValid",
                "c1.commit", "c1.close"), db.calls);
    }

    @Test
    void aDaoCannotEndTheTransactionEarly() {
        connections.inTransaction(Account.HR, () -> {
            try (Connection conn = connections.open(Account.HR)) {
                assertThrows(IllegalStateException.class, conn::commit);
                assertThrows(IllegalStateException.class, conn::rollback);
                assertThrows(IllegalStateException.class, () -> conn.setAutoCommit(true));
            } catch (SQLException e) {
                throw new AssertionError(e);
            }
            return null;
        });

        assertEquals(List.of("c1.setAutoCommit(false)", "c1.commit", "c1.close"), db.calls);
    }

    @Test
    void aTransactionInsideAnotherJoinsItAndOnlyTheOuterOneCommits() {
        connections.inTransaction(Account.HR, () -> connections.inTransaction(Account.HR, () -> "inner"));

        assertEquals(List.of(Account.HR), db.opened);
        assertEquals(List.of("c1.setAutoCommit(false)", "c1.commit", "c1.close"), db.calls);
    }

    @Test
    void aFailureInAnInnerTransactionRollsBackEverything() {
        assertThrows(IllegalStateException.class, () -> connections.inTransaction(Account.HR, () -> {
            useLikeADao(Account.HR); // e.g. the audit row, already written
            return connections.inTransaction(Account.HR, () -> {
                throw new IllegalStateException("inner failed");
            });
        }));

        assertEquals(List.of("c1.setAutoCommit(false)", "c1.isValid", "c1.rollback", "c1.close"), db.calls);
    }

    // ---- outside a transaction, nothing changes from before ----

    @Test
    void outsideATransactionEachOpenIsANewConnectionForThatAccount() throws SQLException {
        assertFalse(connections.isInTransaction());
        try (Connection a = connections.open(Account.EMPLOYEE);
             Connection b = connections.open(Account.HR)) {
            assertFalse(a == b);
        }

        assertEquals(List.of(Account.EMPLOYEE, Account.HR), db.opened);
        assertEquals(List.of("c2.close", "c1.close"), db.calls);
    }

    @Test
    void theTransactionEndsWhenTheBlockEndsEvenAfterAFailure() {
        connections.inTransaction(Account.HR, () -> {
            assertTrue(connections.isInTransaction());
            return null;
        });
        assertFalse(connections.isInTransaction());

        assertThrows(IllegalStateException.class, () -> connections.inTransaction(Account.HR, () -> {
            throw new IllegalStateException("boom");
        }));
        assertFalse(connections.isInTransaction());
    }

    @Test
    void anotherThreadDoesNotJoinThisThreadsTransaction() throws Exception {
        AtomicReference<Throwable> failure = new AtomicReference<>();
        connections.inTransaction(Account.HR, () -> {
            Thread other = new Thread(() -> {
                try (Connection conn = connections.open(Account.EMPLOYEE)) {
                    assertFalse(connections.isInTransaction());
                    assertTrue(conn != null);
                } catch (Throwable t) {
                    failure.set(t);
                }
            });
            other.start();
            try {
                other.join();
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
            return null;
        });

        assertEquals(null, failure.get());
        assertEquals(List.of(Account.HR, Account.EMPLOYEE), db.opened);
    }

    // ---- JdbcTransactionManager ----

    @Test
    void transactionManagerUsesItsOwnAccount() {
        TransactionManager hr = new JdbcTransactionManager(connections, Account.HR);
        List<String> ran = new ArrayList<>();

        hr.runInTransaction(() -> ran.add("work"));

        assertEquals(List.of("work"), ran);
        assertEquals(List.of(Account.HR), db.opened);
        assertEquals(List.of("c1.setAutoCommit(false)", "c1.commit", "c1.close"), db.calls);
    }

    /** What a DAO does: open, use, close. isValid stands in for running a statement. */
    private void useLikeADao(Account account) {
        try (Connection conn = connections.open(account)) {
            conn.isValid(1);
        } catch (SQLException e) {
            throw new AssertionError(e);
        }
    }

    /** Hands out fake connections numbered c1, c2, ... and records every call made on them. */
    private static final class FakeDatabase {
        final List<Account> opened = new ArrayList<>();
        final List<String> calls = new ArrayList<>();
        boolean failOpen;
        boolean failCommit;

        Connection open(Account account) throws SQLException {
            if (failOpen) {
                throw new SQLException("Communications link failure");
            }
            opened.add(account);
            String name = "c" + opened.size();
            return (Connection) Proxy.newProxyInstance(getClass().getClassLoader(),
                    new Class<?>[] {Connection.class}, (proxy, method, args) -> {
                        String call = method.getName();
                        switch (call) {
                            case "setAutoCommit":
                                record(name + ".setAutoCommit(" + args[0] + ")");
                                return null;
                            case "commit":
                                record(name + ".commit");
                                if (failCommit) {
                                    throw new SQLException("Lock wait timeout exceeded");
                                }
                                return null;
                            case "rollback", "close":
                                record(name + "." + call);
                                return null;
                            case "isValid":
                                record(name + ".isValid");
                                return true;
                            case "hashCode":
                                return System.identityHashCode(proxy);
                            case "equals":
                                return proxy == args[0];
                            case "toString":
                                return name;
                            default:
                                throw new UnsupportedOperationException(call);
                        }
                    });
        }

        private synchronized void record(String call) {
            calls.add(call);
        }
    }
}
