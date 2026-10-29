package com.companyz.ems.dao.jdbc;

import com.companyz.ems.config.ConnectionFactory;
import com.companyz.ems.config.DatabaseConfig.Account;
import com.companyz.ems.dao.DataAccessException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Hands JDBC connections to the DAOs, and keeps every DAO call inside a transaction
 * on the same connection.
 *
 * <ul>
 *   <li>Outside a transaction, {@link #open} opens a new connection for the given account.
 *       The caller closes it, as before.</li>
 *   <li>Inside a transaction (on this thread), {@link #open} returns the transaction's
 *       connection. Closing it does nothing, and {@code commit}, {@code rollback} and
 *       {@code setAutoCommit} throw, so a DAO can't end the transaction early.</li>
 * </ul>
 *
 * <p>Create <b>one</b> instance for the whole app and give it to every DAO and every
 * {@link JdbcTransactionManager}. See {@code docs/design/transactions.md}.
 */
public final class ConnectionProvider {

    /** Opens a physical connection. {@link ConnectionFactory#open} in the app; a fake in tests. */
    @FunctionalInterface
    public interface Opener {
        Connection open(Account account) throws SQLException;
    }

    private final Opener opener;
    private final ThreadLocal<Connection> current = new ThreadLocal<>();

    public ConnectionProvider(ConnectionFactory factory) {
        this(Objects.requireNonNull(factory, "factory")::open);
    }

    public ConnectionProvider(Opener opener) {
        this.opener = Objects.requireNonNull(opener, "opener");
    }

    /**
     * A connection for a DAO. Always use it in try-with-resources:
     * {@code try (Connection conn = connections.open(account)) { ... }}
     */
    public Connection open(Account account) throws SQLException {
        Connection tx = current.get();
        if (tx != null) {
            return shared(tx);
        }
        return opener.open(Objects.requireNonNull(account, "account"));
    }

    /** True while this thread is inside {@link #inTransaction}. */
    public boolean isInTransaction() {
        return current.get() != null;
    }

    /**
     * Runs {@code work} in one transaction as {@code account}. If this thread is already in a
     * transaction, {@code work} joins it and the outer transaction decides whether to commit.
     *
     * @throws DataAccessException if the database can't be reached or the commit fails
     *         (nothing is saved in either case)
     */
    public <T> T inTransaction(Account account, Supplier<T> work) {
        Objects.requireNonNull(work, "work");
        if (current.get() != null) {
            return work.get();
        }
        Connection conn = begin(account);
        current.set(conn);
        try {
            T result = work.get();
            conn.commit();
            return result;
        } catch (SQLException commitFailed) {
            rollbackQuietly(conn, commitFailed);
            throw new DataAccessException("Could not save changes; nothing was saved", commitFailed);
        } catch (RuntimeException | Error workFailed) {
            rollbackQuietly(conn, workFailed);
            throw workFailed;
        } finally {
            current.remove();
            closeQuietly(conn);
        }
    }

    private Connection begin(Account account) {
        Connection conn = null;
        try {
            conn = opener.open(Objects.requireNonNull(account, "account"));
            conn.setAutoCommit(false);
            return conn;
        } catch (SQLException e) {
            if (conn != null) {
                closeQuietly(conn);
            }
            throw new DataAccessException("Could not connect to the database", e);
        }
    }

    private static void rollbackQuietly(Connection conn, Throwable reason) {
        try {
            conn.rollback();
        } catch (SQLException | RuntimeException e) {
            reason.addSuppressed(e);
        }
    }

    private static void closeQuietly(Connection conn) {
        try {
            conn.close();
        } catch (SQLException | RuntimeException ignored) {
            // The transaction has already committed or rolled back; nothing more to do.
        }
    }

    /** The transaction's connection as a DAO sees it: close() does nothing, and the DAO can't end the transaction. */
    private static Connection shared(Connection tx) {
        InvocationHandler handler = (proxy, method, args) -> {
            switch (method.getName()) {
                case "close":
                    return null;
                case "commit", "rollback", "setAutoCommit":
                    throw new IllegalStateException(method.getName()
                            + "() is not allowed inside a transaction; the TransactionManager commits or rolls back");
                default:
                    try {
                        return method.invoke(tx, args);
                    } catch (InvocationTargetException e) {
                        throw e.getCause();
                    }
            }
        };
        return (Connection) Proxy.newProxyInstance(
                ConnectionProvider.class.getClassLoader(), new Class<?>[] {Connection.class}, handler);
    }
}
