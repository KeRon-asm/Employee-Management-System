/**
 * MySQL/JDBC implementations of the DAO interfaces in {@code com.companyz.ems.dao}
 * (e.g. {@code JdbcEmployeeDao implements EmployeeDao}).
 *
 * <p>Rules for this package: PreparedStatement only, try-with-resources on every
 * Connection/Statement/ResultSet, and multi-statement changes wrapped in a transaction.
 */
package com.companyz.ems.dao.jdbc;
