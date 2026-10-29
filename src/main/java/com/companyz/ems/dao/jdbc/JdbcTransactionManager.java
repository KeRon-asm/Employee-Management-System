package com.companyz.ems.dao.jdbc;

import com.companyz.ems.config.DatabaseConfig.Account;
import com.companyz.ems.dao.TransactionManager;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * MySQL implementation of {@link TransactionManager}. Every DAO built with the same
 * {@link ConnectionProvider} joins the transaction automatically.
 *
 * <p>Build one per database account: {@code new JdbcTransactionManager(connections, Account.HR)}
 * for HR work, {@code Account.AUTH} for the login lockout.
 */
public final class JdbcTransactionManager implements TransactionManager {

    private final ConnectionProvider connections;
    private final Account account;

    public JdbcTransactionManager(ConnectionProvider connections, Account account) {
        this.connections = Objects.requireNonNull(connections, "connections");
        this.account = Objects.requireNonNull(account, "account");
    }

    @Override
    public <T> T inTransaction(Supplier<T> work) {
        return connections.inTransaction(account, work);
    }
}
