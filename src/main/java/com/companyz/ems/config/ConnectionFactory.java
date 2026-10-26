package com.companyz.ems.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Opens JDBC connections as one of the least-privilege accounts. Callers close them. */
public final class ConnectionFactory {

    private final DatabaseConfig config;

    public ConnectionFactory(DatabaseConfig config) {
        this.config = config;
    }

    public Connection open(DatabaseConfig.Account account) throws SQLException {
        return DriverManager.getConnection(config.url(), config.user(account), config.password(account));
    }
}
