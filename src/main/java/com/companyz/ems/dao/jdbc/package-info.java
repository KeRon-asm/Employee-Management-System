/**
 * MySQL/JDBC implementations of the DAO interfaces in {@code com.companyz.ems.dao}
 * (e.g. {@code JdbcEmployeeDao implements EmployeeDao}).
 *
 * <p>Rules for this package: PreparedStatement only, try-with-resources on every
 * Connection/Statement/ResultSet, and connections come from the shared
 * {@link com.companyz.ems.dao.jdbc.ConnectionProvider}. Never call setAutoCommit, commit or
 * rollback directly; transactions are run by {@link com.companyz.ems.dao.jdbc.JdbcTransactionManager}
 * (see docs/design/transactions.md).
 */
package com.companyz.ems.dao.jdbc;
